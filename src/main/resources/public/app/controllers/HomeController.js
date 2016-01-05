appControllers.controller('HomeController', ['$scope', 'User', function ($scope, User) {
    $scope.users = [];
    $scope.user = {};
    $scope.refresh = function() {
        $scope.users = User.list();
    };
    $scope.submit = function() {
        $scope.userSave = User.save($scope.user)
        $scope.userSave.$promise.then(function() {
            $scope.user = {};
            $scope.refresh();
        });
    };
    $scope.refresh();
}]);