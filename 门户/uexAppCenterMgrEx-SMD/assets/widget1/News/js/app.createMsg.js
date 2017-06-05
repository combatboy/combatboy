/**
 * Created by mitty on 15/12/6.
 */
$(function () {
    var detailStr = localStorage.getItem('News/newsDetail.detail');
    console.log(detailStr);
    var detailObj = JSON.parse(detailStr);
    //暂存专题id
    $('.u-ref-tit-f').text(detailObj.title).data('id', detailObj.id);
    $('.u-ref-info-time').text(detailObj.createdAt.substr(0,16));
    $('.u-ref-info-user').text(detailObj.author);

});
angular.module('zkApp', ['ionic', 'ngUtilSimcere'])
        .service('Data', function () {
            return {
                MsgTitle: '',
                MsgType: '0',//内容类型
                MsgText: '',
                MsgEmflag: false,//紧急标志
                MsgFiflag: false,//直达后台标志
                MsgClassId: '',//消息类型（'0':内部、'1':外部）
                MsgAsflag: false,//重复标志，不填传空串
                MsgCnText: '',//预约时间，16位
                //{ FileName: '', MsgText: ''}
                attaments: [],
                userId: localStorage.getItem('simcere.runtime.userId')
            }
        })
        .service('submit', function (Data, filterData) {
            return function () {
                var params = _.extend({}, Data);
                //todo: 移除fallback
                params.loginId = localStorage.getItem('simcere.runtime.loginId');//||'JS15390';
                params.MsgEmflag = params.MsgEmflag ? '1' : '0';
                params.MsgFiflag = params.MsgFiflag ? '1' : '0';
                params.MsgAsflag = params.MsgAsflag ? '1' : '0';
                params.MsgClassId = filterData.type;//内部、外部

                //取出暂存的专题id
                if(filterData.type==4){
                    var topicId = $('.u-ref-tit-f').data('id');
                    if(topicId!==undefined){
                        params.topicId = topicId;
                    }else{
                        appcan.window.alert('错误', 'topicId丢失');
                        return;
                    }
                }

                //验证必填字段
                if (Data.MsgTitle == '') {
                    appcan.window.openToast('标题不可为空', 2000);
                    return;
                } else if ($.trim(Data.MsgText) == '' || $.trim(Data.MsgText).length < 10) {
                    appcan.window.openToast('内容不少于10个字符', 2000);
                    return;
                } else if (params.MsgClassId === undefined) {
                    //为了避免0值判断为falsely
                    appcan.window.openToast('分类不可为空', 2000);
                    return;
                } else {
                    //number必须转换为string，否则服务器端会将0值判断为空值
                    params.MsgClassId = params.MsgClassId + '';
                }
                console.log(params);

                appcan.window.openToast('请稍候');
                appcan.request.ajax({
                    type: 'POST',
                    url: SimcereConfig.server.mas + '/message/ScrCreateMessage',
                    data: params,
                    contentType: 'application/json',
                    dataType: 'json',
                    success: function (data, status, xhr) {
                        var successMsg = '已成功提交给您的' + ({'0': '上级领导', '1': '后台经理'})[params.MsgFiflag];

                        if (data.status != '0') {
                            appcan.window.openToast(data.msg, 2000);
                        } else {
                            //appcan.window.openToast(data.msg, 2000);
                            appcan.window.openToast(successMsg, 2000);
                            //sync
                            appcan.window.publish('News/createMsg.msgCreated', '');
                            setTimeout(function () {
                                appcan.window.close(-1);
                            }, 2000);
                        }
                    },
                    error: function (xhr, errorType, error) {
                        appcan.window.openToast('网络连接不可用', 2000);
                    }
                });
            };
        })
        .service('fileUploadOkHandler', function (Data) {
            return function (serverPathObj) {
                var scope = angular.element(document.body.firstElementChild).scope();
                scope.$apply(function () {
                    Data.attaments.push({
                        FILEFLAG: window.FILETYPE,
                        FileName: serverPathObj.filename,
                        MsgText: serverPathObj.fileurl
                    });
                });
            };
        })
        .service('uploadFile', function (fileUploadOkHandler) {
            return function (filePath) {
                var uploadUrl = SimcereConfig.server.mas + '/message/ScrUploadAttachment?userId=' + localStorage.getItem('simcere.runtime.userId');
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
                    appcan.window.openToast('上传中');
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
                            appcan.window.closeToast();
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
                                //renderAvatar(serverJson.photo);
                                fileUploadOkHandler.call(null, serverJson);
                            }

                            uexUploaderMgr.closeUploader(opId);

                            break;
                        case 2:
                            //上传失败
                            appcan.window.openToast("上传失败", 2000);
                            uexUploaderMgr.closeUploader(opId);
                            break;
                    }
                };
                //会执行回调： uexUploaderMgr.cbCreateUploader
                uexUploaderMgr.createUploader('2', uploadUrl);
            };
        })
        .service('openCamera', function (uploadFile) {
            return function () {
                uexCamera.cbOpen = function (a, b, c) {
                    uploadFile(c);
                };
                //临时屏蔽手势
                localStorage.setItem('simcere.runtime.doNotCallGesture', '1');
                uexCamera.open();
            };
        })
        .service('openImageBrowser', function (uploadFile) {
            return function () {
                //临时屏蔽手势
                localStorage.setItem('simcere.runtime.doNotCallGesture', '1');
                /*uexImageBrowser.cbPick = function (a, b, c) {
                    uploadFile(c);
                };
                uexImageBrowser.pick();*/
                qlib.openImagePicker(function (url) {
                    uploadFile(url);
                })
            };
        })
        .service('showActionSheet', function ($ionicActionSheet, openCamera, openImageBrowser) {
            return function () {
                window.FILETYPE = '0';

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
                        if (index == 0) {
                            //打开图库
                            openImageBrowser.call();
                        } else if (index == 1) {
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
        .service('fsBrowser', function (uploadFile) {
            return function () {
                /**
                 * @param opId
                 * @param dataType
                 * @param data 返回文件管理器里选择的文件路径
                 */
                uexFileMgr.cbExplorer = function (opId, dataType, data) {
                    window.FILETYPE = '1';
                    uploadFile.call(null, data);
                };
                //临时屏蔽手势
                localStorage.setItem('simcere.runtime.doNotCallGesture', '1');
                uexFileMgr.explorer('/sdcard/widgetone');
            };
        })
        .service('filterData', function () {
            return {
                typeList: [],
                type: 4
            };
        })
        .service('getFilterList', function (filterData) {
            return function () {
                var params = {
                    userId: localStorage.getItem('simcere.runtime.userId')
                };
                var scope = angular.element(document.querySelector('.n_DetailController')).scope();

                appcan.request.ajax({
                    type: 'POST',
                    url: SimcereConfig.server.mas + '/message/ScrQueryMesageByType',
                    data: params,
                    contentType: 'application/json',
                    dataType: 'json',
                    success: function (data, status, xhr) {
                        console.log(data);
                        if (data.status != '0') {
                            //
                        } else {
                            scope.$apply(function () {
                                filterData.typeList = _.reject(data.type, function (o) {
                                    return o.ID == 99;
                                });
                            });
                        }
                    },
                    error: function (xhr, errorType, error) {
                        appcan.window.openToast('网络连接不可用', 2000);
                    }
                });
            };
        })
        .service('managerData', function () {
            return {
                managerName: '',
                managerMsg: '',
                hasManager: ''
            };
        })
        .service('getManager', function (managerData) {
            return function () {
                var params = {
                    userId: localStorage.getItem('simcere.runtime.userId')
                    //userId: 4165
                    //userId: 15102
                };
                var scope = angular.element(document.querySelector('.n_DetailController')).scope();

                appcan.request.ajax({
                    type: 'POST',
                    url: SimcereConfig.server.mas + '/message/ScrQueryManager',
                    data: params,
                    contentType: 'application/json',
                    dataType: 'json',
                    success: function (data, status, xhr) {
                        console.log("@@@@@@@@@后台经理：" + data);
                        if (data.data) {
                            //alert("if"+data.data)
                            scope.$apply(function () {
                                managerData.hasManager = 1;
                                managerData.managerName = data.data[0].MANAGERNAME;
                                managerData.managerMsg = data.msg;
                            });
                        } else {
                            managerData.hasManager = 0;
                            managerData.managerMsg = data.msg;
                        }
                    },
                    error: function (xhr, errorType, error) {
                        appcan.window.openToast('网络连接不可用', 2000);
                    }
                });
            };
        })
        .controller('DetailController', function ($scope, submit, Data, showActionSheet, fsBrowser, imageFilter,
                                                  fileFilter, getFilterList, filterData, getManager, managerData) {
            //
            $scope.owuCloseWin = function () {
                appcan.window.close(-1);
            };

            //数据
            $scope.Data = Data;

            //附件
            $scope.isImgEditMode = true;
            $scope.isFileEditMode = false;
            $scope.activeImgEditMode = function () {
                $scope.isImgEditMode = true;
                uexDevice.vibrate(100);
            };
            $scope.activeFileEditMode = function () {
                $scope.isFileEditMode = true;
                uexDevice.vibrate(100);
            };
            $scope.disableAttachMode = function () {
                $scope.isImgEditMode = false;
                $scope.isFileEditMode = false;
            };
            $scope.removeAttach = function ($event) {
                var file = this.file;
                Data.attaments = _.reject(Data.attaments, function (o) {
                    return o.MsgText == file.MsgText;
                });
                $event.stopPropagation();
            };

            //提交
            $scope.submit = submit;

            //上传图片or文件
            $scope.showActionSheet = showActionSheet;
            $scope.fsBrowser = fsBrowser;

            //过滤
            $scope.imageFilter = imageFilter;
            $scope.fileFilter = fileFilter;

            //服务端类型信息
            $scope.filterData = filterData;
            getFilterList();

            //后台经理
            $scope.managerData = managerData;
            getManager();
        });