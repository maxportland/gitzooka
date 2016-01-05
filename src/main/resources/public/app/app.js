var app = angular.module("app", [
    'ngRoute',
    'appControllers',
    'appServices',
    'ui.bootstrap'
]);

app.config(['$routeProvider', function($routeProvider) {
    $routeProvider.when('/config', {
        templateUrl: 'app/partials/config.html',
        controller: 'ConfigController'
    }).when('/git', {
        templateUrl: 'app/partials/git.html',
        controller: 'GitController'
    }).otherwise({
        redirectTo: '/git'
    });
}]);

app.controller('NavigationController', ['$scope', '$location', function ($scope, $location) {
    $scope.navigation = [
        {'name':'Dashboard', 'path':'/git'},
        {'name':'Configuration', 'path':'/config'}
    ];
    $scope.getPath = function() {
        return $location.path();
    };
}]);

var appControllers = angular.module('appControllers', []);