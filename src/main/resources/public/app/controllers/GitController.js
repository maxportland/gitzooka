appControllers.controller('GitController', ['$scope', '$sce', 'AppRepository', 'AppRepositories', 'GitPull', 'GitPush', 'GitBranches', 'GitCheckout', 'GitBranch', 'GitStatus', 'GitAdd', 'GitRemove', 'GitCommit', function ($scope, $sce, AppRepository, AppRepositories, GitPull, GitPush, GitBranches, GitCheckout, GitBranch, GitStatus, GitAdd, GitRemove, GitCommit) {

    $scope.repoData = [];
    $scope.newBranch = {};
    $scope.createBranchAppName = "";
    $scope.selectedApp = {};
    $scope.appRepositories = [];
    $scope.log = {
        messages:''
    };

    $scope.activeAppTab = "";
    $scope.newApp = {};

    $scope.refresh = function() {
        angular.forEach($scope.appRepositories, function(appRepository) {
            var currentBranch = GitBranch.currentBranch({'appName':appRepository.name});
            currentBranch.$promise.then(function(branch) {
                $scope.repoData[appRepository.name].activeBranch = branch;
                $scope.repoData[appRepository.name].selectedBranch = branch;
                $scope.repoData[appRepository.name].loading = false;
            });
            $scope.repoData[appRepository.name].branches = GitBranches.list({'appName':appRepository.name});
            $scope.getStatus(appRepository.name);
        });
    };

    $scope.appRepositoriesList = AppRepositories.list();
    $scope.appRepositoriesList.$promise.then(function(appRepositories) {
        $scope.appRepositories = appRepositories;
        angular.forEach(appRepositories, function(appRepository) {
            $scope.repoData[appRepository.name] = {
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
        var appRepoSave = AppRepository.save({"path":$scope.newApp.path});
        appRepoSave.$promise.then(function() {
            $scope.appRepositoriesList = AppRepositories.list();
            $scope.appRepositoriesList.$promise.then(function() {
                $scope.refresh();
                $scope.newApp = {};
            });
        });
    }

    $scope.getStatus = function(appName) {
        var status = GitStatus.getStatus({'appName':appName});
        status.$promise.then(function(statusResult) {
            $scope.repoData[appName].status = statusResult;
        });
    };

    $scope.pull = function(path, appName) {
        $scope.repoData[appName].loading = true;
        var pull = GitPull.pull({'path':path});
        pull.$promise.then(function(pullResult) {
            console.log(pullResult);
            if(pullResult.success) {
                $scope.log.messages = $sce.trustAsHtml("Pulled Latest for " + appName + "<br/>"+$scope.log.messages);
            }
            $scope.repoData[appName].loading = false;
        });
    };

    $scope.push = function(appName) {
        $scope.repoData[appName].loading = true;
        var push = GitPush.push({'appName':appName});
        push.$promise.then(function(pushResult) {
            console.log(pushResult);
            $scope.repoData[appName].loading = false;
        });
    };

    $scope.checkout = function(appName) {
        $scope.repoData[appName].loading = true;
        var checkout = GitCheckout.checkout({'branchName':$scope.repoData[appName].selectedBranch.fullName, 'appName':appName});
        checkout.$promise.then(function(branch) {
            $scope.repoData[appName].activeBranch = branch;
            $scope.repoData[appName].selectedBranch = branch;
            $scope.repoData[appName].loading = false;
        });
    };

    $scope.deleteBranch = function(appName) {
        var deleteBranch = GitBranch.delete({'branchName':$scope.repoData[appName].selectedBranch.fullName, 'appName':appName});
        deleteBranch.$promise.then(function(response) {
            console.log(response);
            $scope.refresh();
        });
    };

    $scope.showCreateModal = function(appName) {
        $scope.createBranchAppName = appName;
        $('#create-branch-modal').modal('show');
    };

    $scope.saveNewBranch = function() {
        var newBranch = {
            name:$scope.newBranch.name,
            appName:$scope.createBranchAppName
        };
        var createBranch = GitBranch.create(newBranch);
        createBranch.$promise.then(function(branch) {
            $scope.repoData[$scope.createBranchAppName].activeBranch = branch;
            $scope.repoData[$scope.createBranchAppName].selectedBranch = branch;
            $scope.newBranch = {};
            $scope.createBranchAppName = "";
            $('#create-branch-modal').modal('hide');
            $scope.refresh();
        });
    };

    $scope.showCommitModal = function(appName) {
        $scope.selectedApp = $scope.repoData[appName];
        $('#commit-modal').modal('show');
    };

    $scope.commit = function(appName, message) {
        var gitCommit = GitCommit.commit({'message':message, 'appName':appName});
        gitCommit.$promise.then(function() {
            $scope.getStatus(appName);
            $scope.selectedApp = {};
            $('#commit-modal').modal('hide');
        });
    };

    $scope.add = function(appName, filename) {
        var gitAdd = GitAdd.add({'fileName':filename, 'appName':appName});
        gitAdd.$promise.then(function() {
            $scope.getStatus(appName);
        });
    };

    $scope.remove = function(appName, filename) {
        var gitRemove = GitRemove.remove({'fileName':filename, 'appName':appName});
        gitRemove.$promise.then(function() {
            $scope.getStatus(appName);
        });
    };

}]);