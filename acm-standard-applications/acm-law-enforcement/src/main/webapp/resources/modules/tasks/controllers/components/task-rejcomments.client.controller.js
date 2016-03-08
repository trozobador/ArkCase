'use strict';

angular.module('tasks').controller('Tasks.RejectCommentsController', ['$scope', '$stateParams', '$q'
    , 'UtilService', 'ConfigService', 'ObjectService', 'Object.NoteService', 'Authentication', 'Task.InfoService'
    , 'Helper.UiGridService', 'Helper.ObjectBrowserService'
    , function ($scope, $stateParams, $q
        , Util, ConfigService, ObjectService, ObjectNoteService, Authentication, TaskInfoService
        , HelperUiGridService, HelperObjectBrowserService) {

        var componentHelper = new HelperObjectBrowserService.Component({
            moduleId: "tasks"
            , componentId: "rejcomments"
            , scope: $scope
            , stateParams: $stateParams
            , retrieveObjectInfo: TaskInfoService.getTaskInfo
            , validateObjectInfo: TaskInfoService.validateTaskInfo
            , onConfigRetrieved: function (componentConfig) {
                return onConfigRetrieved(componentConfig);
            }
        });

        var gridHelper = new HelperUiGridService.Grid({scope: $scope});
        var promiseUsers = gridHelper.getUsers();

        var onConfigRetrieved = function (config) {
            $scope.config = config;
            gridHelper.addDeleteButton(config.columnDefs, "grid.appScope.deleteRow(row.entity)");
            gridHelper.setColumnDefs(config);
            gridHelper.setBasicOptions(config);
            gridHelper.disableGridScrolling(config);
            gridHelper.setInPlaceEditing(config, $scope.updateRow);
            gridHelper.setUserNameFilter(promiseUsers);

            $scope.retrieveGridData();
        };

        Authentication.queryUserInfo().then(
            function (userInfo) {
                $scope.userId = userInfo.userId;
                return userInfo;
            }
        );

        $scope.retrieveGridData = function () {
            if (Util.goodPositive(componentHelper.currentObjectId, false)) {
                var promiseQueryNotes = ObjectNoteService.queryRejectComments(ObjectService.ObjectTypes.TASK, componentHelper.currentObjectId);
                $q.all([promiseQueryNotes, promiseUsers]).then(function (data) {
                    var notes = data[0];
                    $scope.gridOptions.data = notes;
                    $scope.gridOptions.totalItems = notes.length;
                    gridHelper.hidePagingControlsIfAllDataShown($scope.gridOptions.totalItems);
                });
            }
        };

        $scope.addNew = function () {
            var lastPage = $scope.gridApi.pagination.getTotalPages();
            $scope.gridApi.pagination.seek(lastPage);
            var newRow = {};
            newRow.parentId = $stateParams.id;
            newRow.parentType = ObjectService.ObjectTypes.CASE_FILE;
            newRow.created = Util.getCurrentDay();
            newRow.creator = $scope.userId;
            $scope.gridOptions.data.push(newRow);
            $scope.gridOptions.totalItems++;
            //gridHelper.hidePagingControlsIfAllDataShown($scope.gridOptions.totalItems);
        };
        $scope.updateRow = function (rowEntity) {
            var note = Util.omitNg(rowEntity);
            ObjectNoteService.saveNote(note).then(
                function (noteAdded) {
                    if (Util.isEmpty(rowEntity.id)) {
                        rowEntity.id = noteAdded.id;
                    }
                }
            );
        };
        $scope.deleteRow = function (rowEntity) {
            gridHelper.deleteRow(rowEntity);

            var id = Util.goodMapValue(rowEntity, "id", 0);
            if (0 < id) {    //do not need to call service when deleting a new row with id==0
                ObjectNoteService.deleteNote(id);
            }

        };

    }
]);