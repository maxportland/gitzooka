package org.rouxium.gitzooka.web;

import org.eclipse.jgit.api.Status;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.diff.DiffEntry;
import org.rouxium.gitzooka.domain.*;
import org.rouxium.gitzooka.service.GitService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("git")
public class GitController {

    @Autowired
    GitService gitService;

    @RequestMapping(value="/repositories", method = RequestMethod.GET, produces = "application/json")
    List<AppRepository> getAppRepositories() {
        return gitService.getAppRepositories();
    }

    @RequestMapping(value="/repository", method = RequestMethod.POST, produces = "application/json")
    AppRepository addRepository(@RequestBody AppRepository appRepository) {
        return gitService.addRepository(appRepository);
    }

    @RequestMapping(value="/pull", method = RequestMethod.GET, produces = "application/json")
    SimplePullResult pull(@RequestParam String repoId) throws IOException, GitAPIException {
        return gitService.pull(repoId);
    }

    @RequestMapping(value="/branch", method = RequestMethod.GET, produces = "application/json")
    Branch getCurrentBranch(@RequestParam String repoId) throws IOException, GitAPIException {
        return gitService.getCurrentBranch(repoId);
    }

    @RequestMapping(value="/branches", method = RequestMethod.GET, produces = "application/json")
    List<Branch> listBranches(@RequestParam String repoId) throws IOException, GitAPIException {
        return gitService.listBranches(repoId);
    }

    @RequestMapping(value="/checkout", method = RequestMethod.GET, produces = "application/json")
    Branch checkoutBranch(@RequestParam String branchName, @RequestParam String repoId) throws IOException, GitAPIException {
        return gitService.checkoutBranch(branchName, repoId);
    }

    @RequestMapping(value="/branch", method = RequestMethod.POST, produces = "application/json")
    Branch createBranch(@RequestBody Branch branch) throws IOException, GitAPIException {
        return gitService.createBranch(branch);
    }

    @RequestMapping(value="/branch", method = RequestMethod.DELETE, produces = "application/json")
    List<String> deleteBranch(@RequestParam String branchName, @RequestParam String repoId) throws IOException, GitAPIException {
        return gitService.deleteBranch(branchName, repoId);
    }

    @RequestMapping(value="/details/changed-files", method = RequestMethod.GET, produces = "application/json")
    List<DiffEntry> getChangedFiles(@RequestParam String repoId) throws IOException, GitAPIException {
        return gitService.getChangedFiles(repoId);
    }

    @RequestMapping(value="/status", method = RequestMethod.GET, produces = "application/json")
    Status getStatus(@RequestParam String repoId) throws IOException, GitAPIException {
        return gitService.getStatus(repoId);
    }

    @RequestMapping(value="/add", method = RequestMethod.POST, produces = "application/json")
    CommitFile addFile(@RequestBody CommitFile file) throws IOException, GitAPIException {
        return gitService.addFile(file);
    }

    @RequestMapping(value="/remove", method = RequestMethod.POST, produces = "application/json")
    CommitFile removeFile(@RequestBody CommitFile file) throws IOException, GitAPIException {
        return gitService.removeFile(file);
    }

    @RequestMapping(value="/commit", method = RequestMethod.POST, produces = "application/json")
    CommitMessage commit(@RequestBody CommitMessage message) throws IOException, GitAPIException {
        return gitService.commit(message);
    }

    @RequestMapping(value="/push", method = RequestMethod.GET, produces = "application/json")
    List<SimplePushResult> push(@RequestParam String repoId) throws IOException, GitAPIException {
        return gitService.push(repoId);
    }

}
