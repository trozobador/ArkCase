'use strict';

angular.module('admin').controller('Admin.ReportsScheduleController', ['$scope', 'Admin.ReportsConfigService', '$q',
    '$translate', 'LookupService', 'Util.DateService', 'Admin.ScheduleReportService', 'MessageService', 'UtilService',
    function($scope, reportsConfigService, $q, $translate, LookupService, UtilDateService,
             ScheduleReportService, MessageService, Util) {

        var deferred = $q.defer();

        // instantiate the promise to pull from acm-reports-server.config.properties
        var promiseServerConfig = LookupService.getConfig("acm-reports-server-config");

        // Containers for dropdown/select options
        $scope.reportTypes = [];
        $scope.reportRecurrence = [];
        $scope.outputTypes = [];
        $scope.hoursList = [];
        // add hours 1-12
        for (var i = 1; i < 13; i++){
            $scope.hoursList.push(i);
        }
        // add minutes 0-59
        $scope.minutesList=[];
        for (var y = 0; y < 60; y++) {
            $scope.minutesList.push(("0" + y).slice(-2));
        }
        // set meridiem options
        $scope.meridiemList=['AM','PM'];

        // wait for promises to resolve
        $q.all([promiseServerConfig]).then(function (payload) {
            // configure the dropdown/select options
            var allProperties = payload[0];
            // value/label pairs are parsed in angular using format "item.label as item.value for item in {list}"
            $scope.reportTypesList = addProperties($scope.reportTypes, allProperties['REPORT_TYPES']);
            $scope.reportRecurrenceList = addProperties($scope.reportRecurrence, allProperties['REPORT_RECURRENCE']);
            $scope.outputTypesList = addProperties($scope.outputTypes, allProperties['REPORT_OUTPUT_TYPES']);

            // Initialize variables
            if (!Util.isArrayEmpty($scope.reportTypesList)) {
                $scope.reportFile = $scope.reportTypesList[0].value;
            }
            $scope.reportRecurrence = 'WEEKLY';
            $scope.reportRecurrenceHours = 12;
            $scope.reportRecurrenceMinutes = '00';
            $scope.reportRecurrenceMeridiem = 'AM';
            $scope.reportStartDate = new Date();
            $scope.reportEmailAddresses = '';
            $scope.outputFormat = 'Excel/CSV';

            // initialize datepickers
            $scope.opened = {};
            $scope.opened.openedStart = false;
            $scope.opened.openedEnd = false;
            $scope.opened.openedFilterStart = false;
            $scope.opened.openedFilterEnd = false;
        });

        function dateToPentahoIso (date, replacement) {
            var replacedWith = (undefined === replacement) ? "" : replacement;

            if (date && date instanceof Date) {
                return moment(date).format("YYYY-MM-DDTHH:mm:ss.SSSZ");
            } else {
                return replacedWith;
            }
        }

        $scope.validateStartDate = function () {
            if ($scope.reportStartDate && $scope.reportRecurrenceHours && $scope.reportRecurrenceMinutes && $scope.reportRecurrenceMeridiem) {
                var startDate = buildDate($scope.reportStartDate, $scope.reportRecurrenceHours, $scope.reportRecurrenceMinutes, $scope.reportRecurrenceMeridiem);
                var currentDate = new Date();
                return startDate > currentDate;
            }
            return false;
        };

        $scope.isSubmitDisabled = function () {
            return Util.isEmpty($scope.reportFile) ||
                Util.isEmpty($scope.filterStartDate) ||
                Util.isEmpty($scope.filterEndDate) ||
                !$scope.validateStartDate();
        };

        $scope.saveNewScheduledReport = function() {
            var selectedReport = _.find($scope.reportTypesList, {value: $scope.reportFile});
            if (!Util.isEmpty(selectedReport)) {
                // process the entered dates
                var startDate = dateToPentahoIso(
                    buildDate(
                        $scope.reportStartDate, $scope.reportRecurrenceHours, $scope.reportRecurrenceMinutes,
                        $scope.reportRecurrenceMeridiem));
                var endDate = dateToPentahoIso($scope.reportEndDate);

                // convert scope variables into an object
                var objectToSubmit = {
                    jobName: selectedReport.label,
                    reportFile: selectedReport.value,
                    uiPassParam: $scope.reportRecurrence,
                    startTime: startDate,
                    endTime: endDate,
                    outputFileType: $scope.outputFormat,
                    emails: $scope.reportEmailAddresses,
                    filterStartDate: UtilDateService.goodIsoDate($scope.filterStartDate),
                    filterEndDate: UtilDateService.goodIsoDate($scope.filterEndDate)
                };
                // parse the object into a JSON string
                var jsonToSubmit = JSON.stringify(objectToSubmit);

                ScheduleReportService.scheduleReport(jsonToSubmit).then(
                    function (data) {
                        $scope.reportEndDate = undefined;
                        $scope.reportEmailAddresses = '';
                        MessageService.info($translate.instant("admin.reports.schedule.createScheduleSuccess"));
                        $scope.$emit('created-report-schedule', data);
                    },
                    function (error) {
                        MessageService.error($translate.instant("admin.reports.schedule.createScheduleFailure"));
                    }
                );
            }
        };

        var addProperties = function(propertyArray, propertyString) {
            // value/label pairs are split by commas in the properties files
            var properties = propertyString.split(',');
            for (var i = 0; i < properties.length; i++) {
                // pairs themselves are split by a dash
                var singlePropertyValuePair = properties[i].split('-');
                var valueLabelPair = {
                    value: singlePropertyValuePair[0],
                    label: singlePropertyValuePair[1]
                };
                propertyArray.push(valueLabelPair);
            }
            return propertyArray;
        };

        // process the entered time and date for the start date
        var buildDate = function(date, hour, minute, meridiem) {
            var adjustedHour = hour;
            var adjustedDate = date;
            // add 12 hours to PM times
            if (adjustedHour < 12 && meridiem === 'PM') {
                adjustedHour = adjustedHour + 12;
            } else {
                // adjust midnight to be hour 00
                if (adjustedHour === 12 && meridiem === 'AM') {
                    adjustedHour = 0;
                }
            }
            adjustedDate.setHours(adjustedHour, minute, 0, 0);
            return adjustedDate;
        };

    }
]);
