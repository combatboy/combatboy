/**
 * Created by mitty on 15/12/4.
 */
angular.module('ngUtilSimcere', [])
        .service('viewImg', function () {
            return function (url) {
                var paramObj = {
                    enableGrid: false,
                    data: [url]
                };
                var paramStr = JSON.stringify(paramObj);
                uexImage.openBrowser(paramStr);
            };
        })
        .service('downloadFile', function () {
            return function (url) {
                //todo: download
                console.log('download file: %s', url);
            };
        })
        .service('imageFilter', function () {
            return function (oneAttach) {
                return oneAttach.FILEFLAG == '0';
            };
        })
        .service('fileFilter', function () {
            return function (oneAttach) {
                return oneAttach.FILEFLAG != '0';
            };
        })
        .filter('toUnixTimestamp', function () {
            return function (b) {
                if(typeof b != 'string'){
                    return '';
                }else{
                    var y = b.substr(0, 4), m = b.substr(4, 2), d = b.substr(6, 2),
                            h = b.substr(8, 2), i = b.substr(10, 2), s = b.substr(12, 2);
                    return new Date(y, m-1, d, h, i, s).getTime();
                }
            };
        })
        .service('showConfirm', function ($ionicPopup) {
            return function (cb, title, content) {
                $ionicPopup.confirm({
                    title: title || '标题',
                    content: content || '内容',
                    cancelText: '取消',
                    okText: '确定'
                }).then(function (res) {
                    if (res) {
                        cb.call();
                    } else {
                        console.log('You are not sure');
                    }
                });
            };
        }).service('showAlert', function ($ionicPopup) {
            return function (title, content, callback){
                $ionicPopup.alert({
                    title: title||'标题',
                    content: content||'内容',
                    cancelText: '取消',
                    okText: '确定'
                }).then(function (res) {
                    if(callback){
                        callback.call(null);
                    }else{
                        console.log('alert callback');
                    }
                });
            };
        });