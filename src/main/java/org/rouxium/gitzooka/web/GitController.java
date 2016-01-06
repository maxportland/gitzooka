package org.rouxium.gitzooka.web;

import org.eclipse.jgit.api.*;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.diff.DiffEntry;
import org.eclipse.jgit.diff.DiffFormatter;
import org.eclipse.jgit.diff.RawTextComparator;
import org.eclipse.jgit.internal.storage.file.FileRepository;
import org.eclipse.jgit.lib.Constants;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.Ref;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevWalk;
import org.eclipse.jgit.transport.SshTransport;
import org.eclipse.jgit.transport.UsernamePasswordCredentialsProvider;
import org.eclipse.jgit.util.io.DisabledOutputStream;
import org.rouxium.gitzooka.config.CustomConfigSessionFactory;
import org.rouxium.gitzooka.domain.*;
import org.rouxium.gitzooka.service.AppRepositoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("git")
public class GitController {

    @Autowired
    AppRepositoryService appRepositoryService;

    @RequestMapping(value="/repositories", method = RequestMethod.GET, produces = "application/json")
    List<AppRepository> getAppRepositories() {
        return appRepositoryService.getAppRepositories();
    }

    @RequestMapping(value="/repository", method = RequestMethod.POST, produces = "application/json")
    AppRepository addRepository(@RequestBody AppRepository appRepository) {
        String name = "";
        if(appRepository.getPath().contains("/")) {
            String[] pathParts = appRepository.getPath().split("/");
            name = pathParts[pathParts.length - 1];
        } else if(appRepository.getPath().contains("\\")) {
            String[] pathParts = appRepository.getPath().split("\\\\");
            name = pathParts[pathParts.length - 1];
        }
        appRepository.setName(name);
        return appRepositoryService.addAppRepository(appRepository);
    }

    @RequestMapping(value="/pull", method = RequestMethod.GET, produces = "application/json")
    SimplePullResult pull(@RequestParam String repoId) throws IOException, GitAPIException {
        AppRepository appRepository = appRepositoryService.getAppRepository(repoId);
        if(appRepository == null) return null;
        Repository localRepo = new FileRepository(appRepository.getPath()+"/.git");
        Git git = new Git(localRepo);
        PullCommand pullCommand = git.pull();
        if(appRepository.getConnectionType().equals(AppRepository.ConnectionType.SSH)) {
            CustomConfigSessionFactory customConfigSessionFactory = appRepositoryService.sessionFactoryFromAppRepository(appRepository);
            pullCommand = pullCommand.setCredentialsProvider(customConfigSessionFactory.getUsernamePasswordCredentialsProvider())
                    .setTransportConfigCallback(transport -> ((SshTransport) transport).setSshSessionFactory(customConfigSessionFactory));
        } else {
            pullCommand = pullCommand.setCredentialsProvider(new UsernamePasswordCredentialsProvider(appRepository.getUsername(), appRepository.getPassword()));
        }
        PullResult pullResult = pullCommand.call();
        return SimplePullResult.builder()
                .fetchMessage(pullResult.getFetchResult().getMessages())
                .mergeSuccess(pullResult.getMergeResult().getMergeStatus().isSuccessful())
                .conflicts(pullResult.getMergeResult().getConflicts())
                .success(pullResult.isSuccessful())
                .build();
    }

    @RequestMapping(value="/branch", method = RequestMethod.GET, produces = "application/json")
    Branch getCurrentBranch(@RequestParam String repoId) throws IOException, GitAPIException {
        AppRepository appRepository = appRepositoryService.getAppRepository(repoId);
        if(appRepository == null) return null;
        Repository localRepo = new FileRepository(appRepository.getPath()+"/.git");
        return Branch.builder().fullName(localRepo.getFullBranch()).name(localRepo.getBranch()).build();
    }

    @RequestMapping(value="/branches", method = RequestMethod.GET, produces = "application/json")
    List<Branch> listBranches(@RequestParam String repoId) throws IOException, GitAPIException {
        AppRepository appRepository = appRepositoryService.getAppRepository(repoId);
        if(appRepository == null) return null;
        Repository localRepo = new FileRepository(appRepository.getPath()+"/.git");
        Git git = new Git(localRepo);
        List<Ref> branches = git.branchList().call();
        List<Branch> branchNames = branches.stream()
                .sorted((branch1, branch2) -> Integer.compare(branch1.hashCode(), branch2.hashCode()))
                .map(branch -> Branch.builder().name(branch.getName().replace("refs/heads/", "")).fullName(branch.getName()).build())
                .filter(branch -> branch.getFullName().contains("refs/heads"))
                .collect(Collectors.toList());
        return branchNames;
    }

    @RequestMapping(value="/checkout", method = RequestMethod.GET, produces = "application/json")
    Branch checkoutBranch(@RequestParam String branchName, @RequestParam String repoId) throws IOException, GitAPIException {
        AppRepository appRepository = appRepositoryService.getAppRepository(repoId);
        if(appRepository == null) return null;
        Repository localRepo = new FileRepository(appRepository.getPath()+"/.git");
        Git git = new Git(localRepo);
        Ref ref = git.checkout()
                .setCreateBranch(false)
                .setName(branchName)
                .setUpstreamMode(CreateBranchCommand.SetupUpstreamMode.NOTRACK)
                .call();
        return Branch.builder().name(ref.getName().replace("refs/heads/", "")).fullName(ref.getName()).build();
    }

    @RequestMapping(value="/branch", method = RequestMethod.POST, produces = "application/json")
    Branch createBranch(@RequestBody Branch branch) throws IOException, GitAPIException {
        AppRepository appRepository = appRepositoryService.getAppRepository(branch.getRepoId());
        if(appRepository == null) return null;
        Repository localRepo = new FileRepository(appRepository.getPath()+"/.git");
        Git git = new Git(localRepo);
        Ref ref = git.branchCreate()
                .setName(branch.getName())
                .setStartPoint("origin/master")
                .setUpstreamMode(CreateBranchCommand.SetupUpstreamMode.NOTRACK)
                .call();
        Ref ref2 = git.checkout()
                .setCreateBranch(false)
                .setName(ref.getName())
                .setUpstreamMode(CreateBranchCommand.SetupUpstreamMode.NOTRACK)
                .call();
        return Branch.builder().name(ref2.getName().replace("refs/heads/", "")).fullName(ref2.getName()).build();
    }

    @RequestMapping(value="/branch", method = RequestMethod.DELETE, produces = "application/json")
    List<String> deleteBranch(@RequestParam String branchName, @RequestParam String repoId) throws IOException, GitAPIException {
        AppRepository appRepository = appRepositoryService.getAppRepository(repoId);
        if(appRepository == null) return null;
        Repository localRepo = new FileRepository(appRepository.getPath()+"/.git");
        Git git = new Git(localRepo);
        List<String> results = git.branchDelete().setBranchNames(branchName).call();
        return results;
    }

    @RequestMapping(value="/details/changed-files", method = RequestMethod.GET, produces = "application/json")
    List<DiffEntry> getChangedFiles(@RequestParam String repoId) throws IOException, GitAPIException {
        AppRepository appRepository = appRepositoryService.getAppRepository(repoId);
        if(appRepository == null) return null;
        Repository localRepo = new FileRepository(appRepository.getPath()+"/.git");
        RevWalk rw = new RevWalk(localRepo);
        ObjectId head = localRepo.resolve(Constants.HEAD);
        RevCommit commit = rw.parseCommit(head);
        RevCommit parent = rw.parseCommit(commit.getParent(0).getId());
        DiffFormatter df = new DiffFormatter(DisabledOutputStream.INSTANCE);
        df.setRepository(localRepo);
        df.setDiffComparator(RawTextComparator.DEFAULT);
        df.setDetectRenames(true);
        List<DiffEntry> diffs = df.scan(parent.getTree(), commit.getTree());
        return diffs;
    }

    @RequestMapping(value="/status", method = RequestMethod.GET, produces = "application/json")
    Status getStatus(@RequestParam String repoId) throws IOException, GitAPIException {
        AppRepository appRepository = appRepositoryService.getAppRepository(repoId);
        if(appRepository == null) return null;
        Repository localRepo = new FileRepository(appRepository.getPath()+"/.git");
        Git git = new Git(localRepo);
        Status status = git.status().call();
        return status;
    }

    @RequestMapping(value="/add", method = RequestMethod.POST, produces = "application/json")
    CommitFile addFile(@RequestBody CommitFile file) throws IOException, GitAPIException {
        AppRepository appRepository = appRepositoryService.getAppRepository(file.getRepoId());
        if(appRepository == null) return null;
        Repository localRepo = new FileRepository(appRepository.getPath()+"/.git");
        Git git = new Git(localRepo);
        git.add().addFilepattern(file.getFileName()).call();
        return file;
    }

    @RequestMapping(value="/remove", method = RequestMethod.POST, produces = "application/json")
    CommitFile removeFile(@RequestBody CommitFile file) throws IOException, GitAPIException {
        AppRepository appRepository = appRepositoryService.getAppRepository(file.getRepoId());
        if(appRepository == null) return null;
        Repository localRepo = new FileRepository(appRepository.getPath()+"/.git");
        Git git = new Git(localRepo);
        git.reset().setRef(Constants.HEAD).addPath(file.getFileName()).call();
        return file;
    }

    @RequestMapping(value="/commit", method = RequestMethod.POST, produces = "application/json")
    CommitMessage commit(@RequestBody CommitMessage message) throws IOException, GitAPIException {
        AppRepository appRepository = appRepositoryService.getAppRepository(message.getRepoId());
        if(appRepository == null) return null;
        Repository localRepo = new FileRepository(appRepository.getPath()+"/.git");
        Git git = new Git(localRepo);
        git.commit().setMessage(message.getMessage()).call();
        return message;
    }

    @RequestMapping(value="/push", method = RequestMethod.GET, produces = "application/json")
    List<SimplePushResult> push(@RequestParam String repoId) throws IOException, GitAPIException {
        AppRepository appRepository = appRepositoryService.getAppRepository(repoId);
        if(appRepository == null) return null;
        Repository localRepo = new FileRepository(appRepository.getPath()+"/.git");
        Git git = new Git(localRepo);
        List<SimplePushResult> simplePushResults = new ArrayList<>();
        PushCommand pushCommand = git.push();
        if(appRepository.getConnectionType().equals(AppRepository.ConnectionType.SSH)) {
            CustomConfigSessionFactory customConfigSessionFactory = appRepositoryService.sessionFactoryFromAppRepository(appRepository);
            pushCommand = pushCommand.setCredentialsProvider(customConfigSessionFactory.getUsernamePasswordCredentialsProvider())
                    .setTransportConfigCallback(transport -> ((SshTransport) transport).setSshSessionFactory(customConfigSessionFactory));
        } else {
            pushCommand = pushCommand.setCredentialsProvider(new UsernamePasswordCredentialsProvider(appRepository.getUsername(), appRepository.getPassword()));
        }
        pushCommand.call().forEach(pushResult -> simplePushResults.add(SimplePushResult.builder().messages(pushResult.getMessages()).build()));
        return simplePushResults;
    }

}
