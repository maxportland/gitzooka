package org.rouxium.gitzooka.service;

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
import org.rouxium.gitzooka.BadRepositoryException;
import org.rouxium.gitzooka.config.CustomConfigSessionFactory;
import org.rouxium.gitzooka.domain.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class GitService {

    @Autowired
    AppRepositoryService appRepositoryService;

    private Repository repositoryFromAppRepository(AppRepository appRepository) throws IOException {
        if (appRepository == null) throw new BadRepositoryException();
        return new FileRepository(appRepository.getPath());
    }

    private Git gitFromAppRepository(AppRepository appRepository) throws IOException {
        return new Git(repositoryFromAppRepository(appRepository));
    }

    public List<AppRepository> getAppRepositories() {
        return appRepositoryService.getAppRepositories();
    }

    public AppRepository addRepository(AppRepository appRepository) {
        String name = "";
        if (appRepository.getPath().contains("/")) {
            String[] pathParts = appRepository.getPath().split("/");
            name = pathParts[pathParts.length - 1];
        } else if (appRepository.getPath().contains("\\")) {
            String[] pathParts = appRepository.getPath().split("\\\\");
            name = pathParts[pathParts.length - 1];
        }
        appRepository.setName(name);
        return appRepositoryService.addAppRepository(appRepository);
    }

    public SimplePullResult pull(String repoId) throws IOException, GitAPIException {
        AppRepository appRepository = appRepositoryService.getAppRepository(repoId);
        Git git = gitFromAppRepository(appRepository);
        PullCommand pullCommand = git.pull();
        if (appRepository.getConnectionType().equals(AppRepository.ConnectionType.SSH)) {
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

    public Branch getCurrentBranch(String repoId) throws IOException, GitAPIException {
        AppRepository appRepository = appRepositoryService.getAppRepository(repoId);
        Repository repository = repositoryFromAppRepository(appRepository);
        return Branch.builder().fullName(repository.getFullBranch()).name(repository.getBranch()).build();
    }

    public List<Branch> listBranches(String repoId) throws IOException, GitAPIException {
        AppRepository appRepository = appRepositoryService.getAppRepository(repoId);
        Git git = gitFromAppRepository(appRepository);
        List<Ref> branches = git.branchList().call();
        return branches.stream()
                .sorted((branch1, branch2) -> Integer.compare(branch1.hashCode(), branch2.hashCode()))
                .map(branch -> Branch.builder().name(branch.getName().replace("refs/heads/", "")).fullName(branch.getName()).build())
                .filter(branch -> branch.getFullName().contains("refs/heads"))
                .collect(Collectors.toList());
    }

    public Branch checkoutBranch(String branchName, String repoId) throws IOException, GitAPIException {
        AppRepository appRepository = appRepositoryService.getAppRepository(repoId);
        Git git = gitFromAppRepository(appRepository);
        Ref ref = git.checkout()
                .setCreateBranch(false)
                .setName(branchName)
                .setUpstreamMode(CreateBranchCommand.SetupUpstreamMode.NOTRACK)
                .call();
        return Branch.builder().name(ref.getName().replace("refs/heads/", "")).fullName(ref.getName()).build();
    }

    public Branch createBranch(Branch branch) throws IOException, GitAPIException {
        AppRepository appRepository = appRepositoryService.getAppRepository(branch.getRepoId());
        Git git = gitFromAppRepository(appRepository);
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

    public List<String> deleteBranch(String branchName, String repoId) throws IOException, GitAPIException {
        AppRepository appRepository = appRepositoryService.getAppRepository(repoId);
        Git git = gitFromAppRepository(appRepository);
        return git.branchDelete().setBranchNames(branchName).call();
    }

    public List<DiffEntry> getChangedFiles(String repoId) throws IOException, GitAPIException {
        AppRepository appRepository = appRepositoryService.getAppRepository(repoId);
        Repository repository = repositoryFromAppRepository(appRepository);
        RevWalk rw = new RevWalk(repository);
        ObjectId head = repository.resolve(Constants.HEAD);
        RevCommit commit = rw.parseCommit(head);
        RevCommit parent = rw.parseCommit(commit.getParent(0).getId());
        DiffFormatter df = new DiffFormatter(DisabledOutputStream.INSTANCE);
        df.setRepository(repository);
        df.setDiffComparator(RawTextComparator.DEFAULT);
        df.setDetectRenames(true);
        return df.scan(parent.getTree(), commit.getTree());
    }

    public Status getStatus(String repoId) throws IOException, GitAPIException {
        AppRepository appRepository = appRepositoryService.getAppRepository(repoId);
        Git git = gitFromAppRepository(appRepository);
        return git.status().call();
    }

    public CommitFile addFile(CommitFile file) throws IOException, GitAPIException {
        AppRepository appRepository = appRepositoryService.getAppRepository(file.getRepoId());
        Git git = gitFromAppRepository(appRepository);
        git.add().addFilepattern(file.getFileName()).call();
        return file;
    }

    public CommitFile removeFile(CommitFile file) throws IOException, GitAPIException {
        AppRepository appRepository = appRepositoryService.getAppRepository(file.getRepoId());
        Git git = gitFromAppRepository(appRepository);
        git.reset().setRef(Constants.HEAD).addPath(file.getFileName()).call();
        return file;
    }

    public CommitMessage commit(CommitMessage message) throws IOException, GitAPIException {
        AppRepository appRepository = appRepositoryService.getAppRepository(message.getRepoId());
        Git git = gitFromAppRepository(appRepository);
        git.commit().setMessage(message.getMessage()).call();
        return message;
    }

    public List<SimplePushResult> push(String repoId) throws IOException, GitAPIException {
        AppRepository appRepository = appRepositoryService.getAppRepository(repoId);
        Git git = gitFromAppRepository(appRepository);
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
