var appServices = angular.module('appServices', ['ngResource']);

appServices.factory('GitBranches', ['$resource', function($resource){
    return $resource('/git/branches', {}, {
        list: {method:'GET', isArray:true}
    });
}]);

appServices.factory('GitCheckout', ['$resource', function($resource){
    return $resource('/git/checkout', {}, {
        checkout: {method:'GET'}
    });
}]);

appServices.factory('GitBranch', ['$resource', function($resource){
    return $resource('/git/branch', {}, {
        currentBranch: {method:'GET'},
        create: {method:'POST'},
        delete: {method:'DELETE', isArray:true}
    });
}]);

appServices.factory('GitStatus', ['$resource', function($resource){
    return $resource('/git/status', {}, {
        getStatus: {method:'GET'}
    });
}]);

appServices.factory('AppRepositories', ['$resource', function($resource){
    return $resource('/git/repositories', {}, {
        list: {method:'GET', isArray:true}
    });
}]);

appServices.factory('AppRepository', ['$resource', function($resource){
    return $resource('/git/repository', {}, {
        save: {method:'POST'}
    });
}]);

appServices.factory('GitPull', ['$resource', function($resource){
    return $resource('/git/pull', {}, {
        pull: {method:'GET'}
    });
}]);

appServices.factory('GitPush', ['$resource', function($resource){
    return $resource('/git/push', {}, {
        push: {method:'GET'}
    });
}]);

appServices.factory('GitAdd', ['$resource', function($resource){
    return $resource('/git/add', {}, {
        add: {method:'POST'}
    });
}]);

appServices.factory('GitRemove', ['$resource', function($resource){
    return $resource('/git/remove', {}, {
        remove: {method:'POST'}
    });
}]);

appServices.factory('GitCommit', ['$resource', function($resource){
    return $resource('/git/commit', {}, {
        commit: {method:'POST'}
    });
}]);