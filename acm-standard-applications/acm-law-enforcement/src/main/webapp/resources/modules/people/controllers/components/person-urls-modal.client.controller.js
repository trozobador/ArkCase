angular.module('people').controller('People.UrlsModalController', ['$scope', '$modalInstance', 'Object.LookupService',
        function ($scope, $modalInstance, ObjectLookupService) {

            ObjectLookupService.getContactMethodTypes().then(
                function (contactMethodTypes) {
                    $scope.urlTypes = _.find(contactMethodTypes, {type: 'url'}).subTypes;
                    return contactMethodTypes;
                });

            $scope.onClickCancel = function () {
                $modalInstance.dismiss('Cancel');
            };
            $scope.onClickOk = function () {
                $modalInstance.close(
                    {
                        url: $scope.url,
                        isDefault: $scope.isDefault,
                        isEdit: $scope.isEdit
                    }
                );
            };
        }
    ]
);