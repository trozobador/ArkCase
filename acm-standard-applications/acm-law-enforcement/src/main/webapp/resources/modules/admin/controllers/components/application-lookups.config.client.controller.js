'use strict';

angular.module('admin').controller('Admin.LookupsConfigController', ['$scope', '$q', '$templateCache', '$http', 'Object.LookupService',
    function ($scope, $q, $templateCache, $http, ObjectLookupService) {
        
        $scope.selectLookupDef = function (selectedLookupDef) {
            $scope.selectedLookupDef = this.selectedLookupDef;
            switch(this.selectedLookupDef.lookupType) {
                case 'standardLookup' :                    
                    $scope.view = "modules/admin/views/components/application-lookups-standard.client.view.html";
                    break;
                case 'nestedLookup' :
                    $scope.view = "modules/admin/views/components/application-lookups-nested-lookup-parent.client.view.html";
                    break;
                case 'inverseValuesLookup' :
                    $scope.view = "modules/admin/views/components/application-lookups-inverse-values.client.view.html";
                    break;
                default:
                    console.error("Unknown lookup type!");
                    break;
            }
            
            $scope.$broadcast('lookup-def-selected', $scope.selectedLookupDef);
        };
                
        ObjectLookupService.getLookupsDefs().then(function(data) {
            $scope.lookupsDefs = data;
            $scope.selectedLookupDef = $scope.lookupsDefs[0];
            $scope.selectLookupDef($scope.selectedLookupDef);
        });
        
        // workaround for the first load of child controllers
        $scope.$on('lookup-controller-loaded', function() {
             $scope.$broadcast('lookup-def-selected', $scope.selectedLookupDef);
        });
    }
]);