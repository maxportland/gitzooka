appControllers.controller('GitController', ['$scope', '$sce', 'AppRepository', 'AppRepositories', 'GitPull', 'GitPush', 'GitBranches', 'GitCheckout', 'GitBranch', 'GitStatus', 'GitAdd', 'GitRemove', 'GitCommit', function ($scope, $sce, AppRepository, AppRepositories, GitPull, GitPush, GitBranches, GitCheckout, GitBranch, GitStatus, GitAdd, GitRemove, GitCommit) {

    $scope.repoData = [];
    $scope.newBranch = {};
    $scope.createBranchAppId = "";
    $scope.selectedApp = {};
    $scope.appRepositories = [];
    $scope.log = {
        messages:''
    };

    $scope.activeAppTab = "";
    $scope.newApp = {};

    $scope.refresh = function() {
        angular.forEach($scope.appRepositories, function(appRepository) {
            var currentBranch = GitBranch.currentBranch({'repoId':appRepository.id});
            currentBranch.$promise.then(function(branch) {
                $scope.repoData[appRepository.id].activeBranch = branch;
                $scope.repoData[appRepository.id].selectedBranch = branch;
                $scope.repoData[appRepository.id].loading = false;
            });
            $scope.repoData[appRepository.id].branches = GitBranches.list({'repoId':appRepository.id});
            $scope.getStatus(appRepository.id);
        });
    };

    $scope.appRepositoriesList = AppRepositories.list();
    $scope.appRepositoriesList.$promise.then(function(appRepositories) {
        $scope.appRepositories = appRepositories;
        angular.forEach(appRepositories, function(appRepository) {
            $scope.repoData[appRepository.id] = {
                id:appRepository.id,
                name:appRepository.name,
                loading:true,
                branches: [],
                status: {},
                activeBranch: {
                    name: '',
                    fullName: ''
                },
                selectedBranch: {
                    name: '',
                    fullName: ''
                }
            };
        });
        $scope.refresh();
    });

    $scope.addApp = function() {
        var appRepoSave = AppRepository.save(
            {
                "path":$scope.newApp.path,
                "username":$scope.newApp.username,
                "password":$scope.newApp.password,
                "privateKeyFile":$scope.newApp.privateKeyFile,
                "knownHostsFile":$scope.newApp.knownHostsFile,
                "privateKeyPassphrase":$scope.newApp.privateKeyPassphrase
            }
        );
        appRepoSave.$promise.then(function() {
            $scope.appRepositoriesList = AppRepositories.list();
            $scope.appRepositoriesList.$promise.then(function() {
                $scope.refresh();
                $scope.newApp = {};
            });
        });
    }

    $scope.getStatus = function(repoId) {
        var status = GitStatus.getStatus({'repoId':repoId});
        status.$promise.then(function(statusResult) {
            $scope.repoData[repoId].status = statusResult;
        });
    };

    $scope.pull = function(repoId) {
        $scope.repoData[repoId].loading = true;
        var pull = GitPull.pull({'repoId':repoId});
        pull.$promise.then(function(pullResult) {
            if(pullResult.success) {
                $scope.log.messages = $sce.trustAsHtml("Pulled Latest for " + $scope.repoData[repoId].name + " / " + $scope.repoData[repoId].activeBranch.name + "<br/>"+$scope.log.messages);
            }
            $scope.repoData[repoId].loading = false;
        });
    };

    $scope.push = function(repoId) {
        $scope.repoData[repoId].loading = true;
        var push = GitPush.push({'repoId':repoId});
        push.$promise.then(function(pushResult) {
            $scope.repoData[repoId].loading = false;
        });
    };

    $scope.checkout = function(repoId) {
        $scope.repoData[repoId].loading = true;
        var checkout = GitCheckout.checkout({'branchName':$scope.repoData[repoId].selectedBranch.fullName, 'repoId':repoId});
        checkout.$promise.then(function(branch) {
            $scope.repoData[repoId].activeBranch = branch;
            $scope.repoData[repoId].selectedBranch = branch;
            $scope.repoData[repoId].loading = false;
        });
    };

    $scope.deleteBranch = function(repoId) {
        var deleteBranch = GitBranch.delete({'branchName':$scope.repoData[repoId].selectedBranch.fullName, 'repoId':repoId});
        deleteBranch.$promise.then(function(response) {
            $scope.refresh();
        });
    };

    $scope.showCreateModal = function(repoId) {
        $scope.createBranchAppId = repoId;
        $('#create-branch-modal').modal('show');
    };

    $scope.showAddRepositoryModal = function(appName) {
        $('#add-repo-modal').modal('show');
    };

    $scope.saveNewBranch = function() {
        var newBranch = {
            name:$scope.newBranch.name,
            repoId:$scope.createBranchAppId
        };
        var createBranch = GitBranch.create(newBranch);
        createBranch.$promise.then(function(branch) {
            $scope.repoData[$scope.createBranchAppId].activeBranch = branch;
            $scope.repoData[$scope.createBranchAppId].selectedBranch = branch;
            $scope.newBranch = {};
            $scope.createBranchAppId = "";
            $('#create-branch-modal').modal('hide');
            $scope.refresh();
        });
    };

    $scope.showCommitModal = function(repoId) {
        $scope.selectedApp = $scope.repoData[repoId];
        $('#commit-modal').modal('show');
    };

    $scope.commit = function(repoId, message) {
        var gitCommit = GitCommit.commit({'message':message, 'repoId':repoId});
        gitCommit.$promise.then(function() {
            $scope.getStatus(repoId);
            $scope.selectedApp = {};
            $('#commit-modal').modal('hide');
        });
    };

    $scope.add = function(repoId, filename) {
        var gitAdd = GitAdd.add({'fileName':filename, 'repoId':repoId});
        gitAdd.$promise.then(function() {
            $scope.getStatus(repoId);
        });
    };

    $scope.remove = function(repoId, filename) {
        var gitRemove = GitRemove.remove({'fileName':filename, 'repoId':repoId});
        gitRemove.$promise.then(function() {
            $scope.getStatus(repoId);
        });
    };

}]);