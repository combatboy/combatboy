/**
 * Created by mitty on 15/9/8.
 */
angular.module('zkApp', ['ionic'])
        .directive('bgAvatar', function () {
            return function (scope, element, attrs) {
                var url = attrs.bgAvatar;
                element.css({
                    'background-image': 'url(' + url + ')'
                });
            };
        })
        .filter('formatNumber', function () {
            /**
             * @param number 手机号
             * @param isVis 可见性
             */
            return function (number, isVis) {
                var a = [];
                a.push('+86', number.substr(0, 3), number.substr(3, 4), number.substr(7, 4));
                //仅当用户明确设置了“不可见”的情况下隐藏手机号
                console.log("isVis: %s | %s", typeof isVis, isVis);
                //alert("isVis:"+typeof isVis+":"+isVis)
                //需求变更：总是用***替换手机号的4-7位
                //if(isVis===0){
                a[2] = (a[2] || '').replace(/[\s\S]/g, '*');
                //}
                return a.join(' ');
            }
        })
        .filter('escapeEmail', function () {
            return function (mail) {
                var s = mail.replace(/(.*).{3}(@.*)/, '$1***$2');
                if (s.indexOf('*') == -1) {
                    var f = mail.match(/(.*)(@.*)/);
                    s = f[1].replace(/(.)/g, '*') + f[2];
                }
                return s;
            }
        })
        .filter('bgWrap', function () {
            return function (url) {
                return url ? 'background-image:url(' + url + ');' : '';
            };
        })
        .filter('trimLeft', function () {
            var trimLeft = function (str, remove) {
                var regCustom = new RegExp('^' + remove + '+'),
                        regEmpty = /^\s+/;
                var reg = remove === undefined ? regEmpty : regCustom;
                return str.replace(reg, '');
            };
            return function (str, remove) {
                var res = '';
                if (typeof str == 'string') {
                    res = trimLeft(str, remove);
                }
                return res;
            }
        })
        .filter('filterObjStrRepresentation', function () {
            //过滤字符串"[object Object]"
            var objectString = ({}).toString();
            return function (s) {
                return s == objectString ? '' : s;
            };
        })
        .service('getUserId', function () {
            return function () {
                return localStorage.getItem('simcere.runtime.userId');
            };
        }).service('renderPhoto', function () {
            //跨页面版本
            return function (url) {
                appcan.window.publish('Home_index.hasPhoto', url);
            };
        }).service('getDetail', function (renderPhoto, renderAvatar) {
            /**
             * 请求
             */
            return function (userId, _$scope) {
                console.log('personIdView: %s', userId);

                var typeof_userId = typeof userId;
                //参数检查
                if (typeof_userId != 'string') {
                    throw new TypeError('expected parameter "psnId" to be string,' + typeof_userId + ' provided')
                } else if (userId.length == 0) {
                    throw new TypeError('"psnId" can not be a empty string');
                }

                var params = {
                    "userId": userId
                };
                appcan.window.openToast('请稍候');
                appcan.request.ajax({
                    type: 'POST',
                    url: SimcereConfig.server.mas + '/contact/ScrQueryUserDetail',
                    data: params,
                    contentType: 'application/json',
                    dataType: 'json',
                    success: function (data, status, xhr) {
                        appcan.window.closeToast();
                        //头像
                        if (data.photo && data.photo.photo) {
                            renderAvatar.call(null, data.photo.photo);
                        }

                        //强制异步，避免inprog错误
                        setTimeout(function () {
                            _$scope.$apply(function () {
                                _$scope.person = data.user;
                            });
                        }, 0);

                        /*setTimeout(function () {
                         AvatarCacheMgr.sync();
                         }, 100);*/
                    },
                    error: function (xhr, errorType, error) {
                        //todo:errorHandler
                        if (!_$scope.person) {
                            appcan.window.openToast('网络连接不可用', 2000);
                        }
                    }
                });

            }
        }).service('logoutHandler', function () {
            var logoutHelper = function () {
                //解绑用户
                uexEMM.cbGetSoftToken=function(a,b,c){
                    var url=SimcereConfig.server.emm+'/push/msg/'+c+'unBindUser';
                    $.getJSON(url, function(){
                        console.info('解绑用户，关闭推送 cb1');
                    }, 'json', function(){
                        console.warn('解绑用户，关闭推送 cb2');
                    }, 'POST','','');
                };
                uexEMM.getSoftToken();
                setTimeout(function () {
                //移除相应标记
                localStorage.removeItem('simcere.runtime.hasGesturePassword');
                localStorage.removeItem('simcere.runtime.isScreenLocked');
                //打开登录窗口
                appcan.window.open("Auth_index", "../../Auth/html/index.html", 10);
                }, 200);
            };
            return function () {
                //todo:此处的时序不严谨
                //登出EMM
                try{
                    uexEMM.logout();
                    uexIM.logout();
                }catch(ex){
                    alert(ex.message);
                }
                appcan.window.openToast('请稍候');
                //清除session
                appcan.request.ajax({
                    type: 'POST',
                    url: SimcereConfig.server.mas + '/base/clearSession',
                    data: {},
                    contentType: 'application/json',
                    dataType: 'json',
                    success: function (data, status, xhr) {
                        appcan.window.closeToast();
                        logoutHelper.call();
                    },
                    error: function (xhr, errorType, error) {
                        //todo: 操作失败
                        appcan.window.closeToast();
                        console.error('fail to clearSession');
                        logoutHelper.call();
                    }
                });


            };
        })
        .service('renderAvatar', function () {
            return function (d) {
                var img = new Image();
                img.onload = function () {
                    $('.n_avatar').css('background-image', 'url(' + d + ');');
                };
                img.src = d;
            }
        })
        .service('changeAvatarOnBBS', function () {
            /**
             * 更新bbs中的头像
             * @param photoUrl
             */
            return function (photoUrl) {
                var currentLoginId = localStorage.getItem('simcere.runtime.loginId');
                var params = {
                    "username": currentLoginId,
                    "avatar": photoUrl
                };
                appcan.request.ajax({
                    type: 'POST',
                    url: SimcereConfig.server.bbs + 'home.php?mod=spacecp&ac=editavatar',
                    data: params,
                    //contentType: 'application/json',
                    dataType: 'text',
                    success: function (data, status, xhr) {
                        var res;
                        try{
                            res = eval(data);
                        }catch(ex){}
                        if(res.status=='修改成功'){
                            console.log('bbs头像同步成功');
                        }else{
                            console.error('bbs头像同步失败');
                        }
                    },
                    error: function (xhr, errorType, error) {
                        console.error('bbs头像同步失败：' + error);
                    }
                });
            };
        })
        .service('uploadAvatar', function (renderAvatar, changeAvatarOnBBS) {
            return function (filePath) {
                var uploadUrl = SimcereConfig.server.mas + '/contact/ScrUploadUserPhoto?userId=' + localStorage.getItem('simcere.runtime.userId');
                /**
                 * uexUploaderMgr.cbCreateUploader
                 * @param {number} opId
                 * @param {string} serverURL
                 */
                uexUploaderMgr.cbCreateUploader = function (opId, serverURL) {
                    uexUploaderMgr.setHeaders(opId, '{}');
                    /**
                     * uexUploaderMgr.uploadFile
                     * @param {number} opId
                     * @param {string} filePath
                     * @param {string} inputName
                     * @param {number} quality
                     * @param {number} maxWidth
                     */
                        //开始上传
                    uexUploaderMgr.uploadFile(opId, filePath, 'file', 0, 1000);
                };
                /**
                 * uexUploaderMgr.onStatus
                 * @param {number} opId
                 * @param {number} fileSize
                 * @param {number} percent
                 * @param {number} serverPath
                 * @param {number} status
                 */
                uexUploaderMgr.onStatus = function (opId, fileSize, percent, serverPath, status) {
                    var serverJson;
                    switch (status) {
                        case 0:
                            //上传中
                            break;
                        case 1:
                            //上传成功
                            try {
                                serverJson = JSON.parse(serverPath);
                            } catch (ex) {
                                //json解析失败，理论上不会出现此情况
                            }
                            if (typeof serverJson != 'object') {
                                //json解析失败，理论上不会出现此情况
                            } else if (serverJson.status != '0') {
                                //上传图片失败
                                appcan.window.openToast(serverJson.msg, 2000);
                            } else {
                                renderAvatar(serverJson.photo);
                                changeAvatarOnBBS(serverJson.photo);
                            }

                            uexUploaderMgr.closeUploader(opId);

                            break;
                        case 2:
                            //上传失败
                            alert("上传失败");
                            uexUploaderMgr.closeUploader(opId);
                            break;
                    }
                };
                //会执行回调： uexUploaderMgr.cbCreateUploader
                uexUploaderMgr.createUploader('1', uploadUrl);

            };
        })
        .service('clipImage', function (uploadAvatar) {
            return function (d) {
                uexClipImage.cbGetImage = function (a,b,c) {
                    uploadAvatar(c);
                };
                uexClipImage.open(d);
            };
        })
        .service('openCamera', function (clipImage) {
            return function () {
                uexCamera.cbOpen = function (a,b,c) {
                    clipImage(c);
                };
                //临时屏蔽手势
                localStorage.setItem('simcere.runtime.doNotCallGesture', '1');
                uexCamera.open();
            };
        })
        .service('openImageBrowser', function (clipImage) {
            return function () {
                //临时屏蔽手势
                localStorage.setItem('simcere.runtime.doNotCallGesture', '1');
                /*uexImageBrowser.cbPick = function (a,b,c) {
                    clipImage(c);
                };
                uexImageBrowser.pick();*/
                qlib.openImagePicker(function (url) {
                    clipImage(url);
                })
            };
        })
        .service('showActionSheet', function ($ionicActionSheet, openCamera, openImageBrowser) {
            return function () {
                $ionicActionSheet.show({
                    buttons: [
                        {
                            text: '本地上传'
                        },
                        {
                            text: '拍照上传'
                        }
                    ],
                    cancelText: '取消',
                    cancel: function () {
                        console.log('CANCELLED');
                    },
                    buttonClicked: function (index) {
                        if(index==0){
                            //打开图库
                            openImageBrowser.call();
                        }else if(index==1){
                            //打开相机
                            openCamera.call();
                        }
                        return true;
                    },
                    destructiveButtonClicked: function () {
                        console.log('DESTRUCT');
                        return true;
                    }
                });
            };
        })
        .service('openSettings', function () {
            //打开设置页面
            return function () {
                appcan.window.open("Home_set", "set.html", 10);
            };
        })
        .controller('PersonalDetailController', function ($scope, getUserId, getDetail, logoutHandler, showActionSheet, openSettings) {
            $scope.logout = logoutHandler;
            //声明变量
            $scope.person = {};
            getDetail(getUserId(), $scope);

            $scope.showActionSheet = showActionSheet;
            $scope.openSettings = openSettings;

        });