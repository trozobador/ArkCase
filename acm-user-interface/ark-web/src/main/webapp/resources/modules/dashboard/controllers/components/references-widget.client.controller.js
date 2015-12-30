'use strict';

angular.module('dashboard.references', ['adf.provider'])
    .config(function (dashboardProvider) {
        dashboardProvider
            .widget('references', {
                    title: 'References Widget',
                    description: 'Displays references',
                    controller: 'Dashboard.ReferencesController',
                    reload: true,
                    templateUrl: 'modules/dashboard/views/components/references-widget.client.view.html'
                }
            );
    })
    .controller('Dashboard.ReferencesController', ['$scope', '$translate', '$stateParams', 'UtilService', 'Case.InfoService', 'Complaint.InfoService','Authentication', 'Dashboard.DashboardService',
        function ($scope, $translate, $stateParams, Util, CaseInfoService, ComplaintInfoService, Authentication, DashboardService) {

            $scope.$on('component-config', applyConfig);
            $scope.$emit('req-component-config', 'main');
            $scope.config = null;
            //var userInfo = null;

            $scope.gridOptions = {
                enableColumnResizing: true,
                columnDefs: []
            };

            function applyConfig(e, componentId, config) {
                if (componentId == 'main') {
                    $scope.config = config;
                    $scope.gridOptions.columnDefs = config.widgets[6].columnDefs; //widget[6] = references
                    //set gridOptions.data
                    if ($stateParams.type) {
                        if ($stateParams.type == "casefile") {
                            CaseInfoService.getCaseInfo($stateParams.id).then(
                                function (data) {
                                    $scope.gridOptions.data = data.references;
                                    $scope.gridOptions.totalItems = $scope.gridOptions.data.length;
                                }
                                , function (error) {
                                    $scope.caseInfo = null;
                                    $scope.progressMsg = $translate.instant("cases.progressError") + " " + id;
                                    return error;
                                }
                            );
                        }
                        else if ($stateParams.type == 'complaint') {
                            ComplaintInfoService.getComplaintInfo($stateParams.id).then(
                                function (data) {
                                    $scope.gridOptions.data = data.references;
                                    $scope.gridOptions.totalItems = $scope.gridOptions.data.length;
                                }
                                , function (error) {
                                    $scope.complaintInfo = null;
                                    $scope.progressMsg = $translate.instant("complaint.progressError") + " " + id;
                                    return error;
                                }
                            );
                        }
                        else {
                            //do nothing
                        }
                    }
                }
            }
        }
    ]);