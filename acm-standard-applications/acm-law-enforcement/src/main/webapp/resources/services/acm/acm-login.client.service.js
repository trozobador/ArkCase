'use strict';

/**
 * @ngdoc service
 * @name services:Acm.LoginService
 *
 * @description
 *
 * {@link https://gitlab.armedia.com/arkcase/ACM3/tree/develop/acm-standard-applications/acm-law-enforcement/src/main/webapp/resources/services/acm/login.client.service.js services/acm/login.client.service.js}
 *
 * This service is used to manage login. It holds login information, for example login status, idle time, error statistics, etc.
 */

angular.module('services').factory('Acm.LoginService', ['$q', '$state', '$injector', '$log'
    , 'Acm.StoreService', 'UtilService', 'ConfigService'
    , function ($q, $state, $injector, $log
        , Store, Util, ConfigService
    ) {
        var Service = {
            LocalCacheNames: {
                LOGIN_STATUS: "AcmLoginStatus"
            }

            /**
             * @ngdoc method
             * @name getSetLoginPromise
             * @methodOf services:Acm.LoginService
             *
             * @description
             * Return a promise to wait for setLogin(true) is called. If it is already login, it return true to
             * indicate the promise is already been resolved to true.
             */
            , getSetLoginPromise: function() {
                var login = Service.isLogin();
                if (login) {
                    return true;
                }

                return Service._deferSetLogin.promise;
            }


            /**
             * @ngdoc method
             * @name isLogin
             * @methodOf services:Acm.LoginService
             *
             * @description
             * Return boolean to indicate if current session is in login status
             */
            , isLogin: function () {
                var cacheLoginStatus = new Store.LocalData(Service.LocalCacheNames.LOGIN_STATUS);
                var loginStatus = cacheLoginStatus.get();
                return Util.goodMapValue(loginStatus, "login", false);
            }

            /**
             * @ngdoc method
             * @name setLogin
             * @methodOf services:Acm.LoginService
             *
             * @param {Boolean} login Login status as boolean. true if login; false if not
             *
             * @description
             * Set login status
             */
            , setLogin: function (login) {
                var cacheLoginStatus = new Store.LocalData(Service.LocalCacheNames.LOGIN_STATUS);
                var loginStatus = Util.goodValue(cacheLoginStatus.get(), {});
                loginStatus.login = login;
                cacheLoginStatus.set(loginStatus);
                if (login) {
                    this._deferSetLogin.resolve(login);
                }
            }

            /**
             * @ngdoc method
             * @name getLastIdle
             * @methodOf services:Acm.LoginService
             *
             * @description
             * Get last idle time
             */
            , getLastIdle: function () {
                var cacheLoginStatus = new Store.LocalData(Service.LocalCacheNames.LOGIN_STATUS);
                var loginStatus = Util.goodValue(cacheLoginStatus.get(), {});
                return loginStatus.lastIdle = Util.goodMapValue(loginStatus, "lastIdle", new Date().getTime());
            }

            /**
             * @ngdoc method
             * @name setLastIdle
             * @methodOf services:Acm.LoginService
             *
             * @param {Boolean} (optional)val Last idle time in seconds. If not specified, current time is used
             *
             * @description
             * Set last user active time to mark as beginning of an idle period.
             */
            , setLastIdle: function (val) {
                var cacheLoginStatus = new Store.LocalData(Service.LocalCacheNames.LOGIN_STATUS);
                var loginStatus = Util.goodValue(cacheLoginStatus.get(), {});
                loginStatus.lastIdle = Util.goodValue(val, new Date().getTime());
                cacheLoginStatus.set(loginStatus);
            }

            /**
             * @ngdoc method
             * @name getSinceIdle
             * @methodOf services:Acm.LoginService
             *
             * @description
             * Set time elapses since last idle
             */
            , getSinceIdle: function () {
                var last = this.getLastIdle();
                var now = new Date().getTime();
                return now - last;
            }


            /**
             * @ngdoc method
             * @name resetCaches
             * @methodOf services:Acm.LoginService
             *
             * @description
             * Reset caches to get ready for new user or for next user
             */
            , resetCaches: function () {
                ConfigService.getModuleConfig("common").then(function (moduleConfig) {
                    var resetCacheNames = Util.goodMapValue(moduleConfig, "resetCacheNames", []);
                    _.each(resetCacheNames, function(cacheList) {
                        var type = Util.goodMapValue(cacheList, "type");
                        var names = Util.goodMapValue(cacheList, "names");
                        if ("session" == Util.goodMapValue(cacheList, "type")) {
                            try {
                                var service = $injector.get(Util.goodMapValue(cacheList, "service"));
                                var SessionCacheNames = Util.goodMapValue(service, Util.goodMapValue(cacheList, "names"), {});
                                _.each(SessionCacheNames, function (name) {
                                    var cache = new Store.SessionData(name);
                                    cache.set(null);
                                });
                            } catch(e) {
                                $log.error("AcmLoginService: " + err.message);
                            }
                        }
                    });

                    return moduleConfig;
                });

                //Above ConfigService.getModuleConfig() just created a cache, reset it as well
                var cache = new Store.SessionData(ConfigService.SessionCacheNames.MODULE_CONFIG_MAP);
                cache.set(null);
            }


            /**
             * @ngdoc method
             * @name logout
             * @methodOf services:Acm.LoginService
             *
             * @description
             * Set off logout. Currently, it route to 'goodbye' page
             */
            , logout: function () {
                $state.go("goodbye");
            }
        };

        Service._deferSetLogin = $q.defer();

        return Service;
    }
]);