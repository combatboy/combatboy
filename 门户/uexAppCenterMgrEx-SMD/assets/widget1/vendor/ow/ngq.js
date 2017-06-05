/**
 * Created by mitty on 16/3/11.
 */
angular.module('ngq', [])
        .filter('timestamp', function () {
            return function (date) {
                var arr = date.split(/[- :]/),
                        dt = new Date(arr[0], arr[1]-1, arr[2], arr[3], arr[4], arr[5]);
                return dt.getTime();
            };
        })
        .filter('substr', function () {
            return function (str, index, len, elps) {
                var tmp = str.substr(index, len);
                if (elps !== undefined && str.length > len) {
                    tmp += elps;
                }
                return tmp;
            };
        })
        .service('ngqJson', function () {
            return {
                ls2j: function (key) {
                    var s = localStorage.getItem(key);
                    if (s === null) {
                        console.warn('localStorage with key "' + key + '" doesn\'t exists.');
                    }
                    return s === null ? {} : angular.fromJson(s);
                },
                j2ls: function (key, plainObj) {
                    var tmp = typeof plainObj == 'object' ? angular.toJson(plainObj) : plainObj;
                    localStorage.setItem(key, tmp);
                }
            };
        })
        .service('ngqOpenImageBrowser', function () {
            return function (cb) {
                /*此插件存在bug，并已停止维护
                 uexImageBrowser.cbPick = function (opId, dataType, data) {
                 console.log('uexImageBrowser.cbPick selected: ' + data);
                 cb(data);
                 };
                 uexImageBrowser.pick();*/

                /**
                 * 打开图片选择视图
                 * @param data.isCancelled
                 * @param data.detailedImageInfo
                 * @param data.data
                 */
                uexImage.onPickerClosed=function(data){
                    console.log('uexImage.onPickerClosed: ' + data);
                    var res = JSON.parse(data);
                    if(res.isCancelled){
                        console.log('uexImage.isCancelled');
                    }else if(!res.data.length){
                        console.warn('图片列表为空');
                    }else{
                        //目前只支持单张上传
                        cb(res.data[0]);
                    }
                };
                var paramObj = {
                    min: 1,
                    max: 1,
                    quality: 1,
                    usePng: false,
                    detailedInfo: false
                };
                var paramStr = JSON.stringify(paramObj);
                uexImage.openPicker(paramStr);
            };
        })
        .service('ngqOpenCamera', function () {
            return function (cb) {
                uexCamera.cbOpen = function (opId, dataType, data) {
                    console.log('uexCamera.cbOpen image took: ' + data);
                    cb(data);
                };
                uexCamera.open();
            };
        })
        .service('ngqUploadFile', function () {
            /**
             * @param paramObj.filePath {string}
             * @param paramObj.serverUrl {string}
             * @param paramObj.progress {function}
             * @param paramObj.success {function}
             * @param paramObj.error {function}
             */
            return function (paramObj) {
                /**
                 * uexUploaderMgr.cbCreateUploader
                 * @param {number} opId
                 * @param {string} serverURL
                 */
                uexUploaderMgr.cbCreateUploader = function (opId, serverURL) {
                    //uexUploaderMgr.setHeaders(opId, '{}');
                    /**
                     * 开始上传
                     * uexUploaderMgr.uploadFile
                     * @param {number} opId
                     * @param {string} filePath
                     * @param {string} inputName
                     * @param {number} quality
                     * @param {number} maxWidth
                     */
                    console.log('uexUploaderMgr.uploadFile with params: ' + 'opid=>' + opId + ', filePath: ' + paramObj.filePath);
                    uexUploaderMgr.uploadFile(opId, paramObj.filePath, 'file', 0, 1000);
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
                    switch (status) {
                        case 0:
                            //上传中
                            if (paramObj.progress) {
                                paramObj.progress.call(null, percent);
                            }
                            break;
                        case 1:
                            if (paramObj.success) {
                                paramObj.success.call(null, serverPath);
                            }
                            //延迟处理
                            setTimeout(function () {
                                uexUploaderMgr.closeUploader(opId);
                            }, 500);
                            break;
                        case 2:
                            //上传失败
                            if (paramObj.error) {
                                paramObj.error.call(null, opId);
                            }
                            uexUploaderMgr.closeUploader(opId);
                            break;
                    }
                };
                //会执行回调： uexUploaderMgr.cbCreateUploader
                uexUploaderMgr.createUploader(_.random(0, 999999), paramObj.serverUrl);
            };
        })
        .service('ngqActionSheet', function () {
            return function (cb, buttons, style) {
                uexActionSheet.onClickItem = function (index) {
                    console.log('ngqActionSheet selected index: ' + index);
                    cb.call(null, index);
                };
                //按钮列表格式化
                var objButtons = _.map(buttons, function (name) {
                    return {name: name};
                });
                var styleObj = {
                    actionSheet_style: {
                        frameBgColor: "#FFFFFF",//背景色
                        frameBroundColor: "#000000",//边框颜色
                        frameBgImg: "",//背景图
                        btnSelectBgImg: "res://actionSheet/btn-act.png",//一般按钮被选中的背景图
                        btnUnSelectBgImg: "res://actionSheet/btn.png",//一般按钮未被选中的背景图
                        cancelBtnSelectBgImg: "res://actionSheet/cancel-act.png",//取消按钮 被选中的背景图
                        cancelBtnUnSelectBgImg: "res://actionSheet/cancel.png",//取消按钮 未被选中的背景图
                        textSize: "17",//文字字号
                        textNColor: "#ffffff",//一般按钮,未被选中状态下的文字颜色
                        textHColor: "#ffff00",//一般按钮,被选中状态下的文字颜色
                        cancelTextNColor: "#ffffff",//取消按钮,未被选中状态下的文字颜色
                        cancelTextHColor: "#ffffff",//取消按钮,被选中状态下的文字颜色
                        // [{ name: "本地上传"}, { name: "拍照上传"}]
                        actionSheetList: objButtons
                    }
                };
                var jsonData = JSON.stringify(styleObj);
                console.log(jsonData);
                /**
                 * @param x {Number} required x坐标
                 * @param y {Number} required y坐标(已失效,请传0)
                 * @param width {Number} required 宽度,如果传0,默认是屏幕宽度
                 * @param height {Number} required 高度(已失效,请传0)
                 * @param jsonData {String} required 按钮内容
                 */
                uexActionSheet.open(0, 0, 0, 0, jsonData);
            };
        })
        .service('ngqRemoveItem', function () {
            return function (arr, target){
                var i = arr.indexOf(target);
                arr.splice(i,1);
            };
        })
        .service('ngqViewImages', function () {
            /**
             * @params o.urls
             * @params o.url
             * @params o.index
             */
            return function (o) {
                var imageUrlSet = o.urls;
                var activeIndex = 0;

                if (o.index) {
                    activeIndex = o.index;
                } else if (o.url) {
                    activeIndex = o.urls.indexOf(o.url);
                }

                if(activeIndex<0){
                    activeIndex = 0;
                }

                console.log(imageUrlSet, activeIndex);

                //此插件已经被废弃
                //uexImageBrowser.open(imageUrlSet, activeIndex);
                var paramObj = {
                    displayActionButton: false,
                    displayNavArrows: false,
                    enableGrid: false,
                    startOnGrid: false,
                    startIndex: activeIndex,
                    data: imageUrlSet
                };
                var paramStr = JSON.stringify(paramObj);
                uexImage.openBrowser(paramStr);
            };
        });