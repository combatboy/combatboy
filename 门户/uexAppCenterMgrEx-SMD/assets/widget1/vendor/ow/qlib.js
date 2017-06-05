/**
 * Created by mitty on 16/3/8.
 */
(function (w) {
    function j2s(json){
        return JSON.stringify(json);
    }
    function s2j(str){
        return JSON.parse(str);
    }


    function json2localStorage(key, plainObj) {
        var tmp = typeof plainObj == 'object' ? JSON.stringify(plainObj) : plainObj;
        localStorage.setItem(key, tmp);
    }

    function localStorage2json(key) {
        var s = localStorage.getItem(key);
        if (s === null) {
            console.warn('localStorage with key "' + key + '" doesn\'t exists.');
        }
        return s === null ? {} : JSON.parse(s);
    }

    /**
     * 日历插件
     * @param cb {function} 回调函数
     * @param openType {String} "1"：选择一个日期；"2"：返回两个日期
     * @param dateType {String} 有效时段，"0"今天，前后5年；"1"：今天到一年后；
     * @param dataSelect {JSON} 可选，初始化时已经选择的日期，{"begin":"2015-05-06","end":"2015-05-08"}
     */
    function selectDate(cb, openType, dateType, dataSelect){
        if (typeof uexCalendar == 'undefined') {
            cb('{"begin":"2011-02-22","end":"2017-02-22"}');
            console.warn('模拟uexCalendar');
            return;
        }

        //@params
        uexCalendar.cbCallBack = function (opId, dataType, data) {
            cb.call(uexCalendar, data);
        };
        //格式化参数
        var arg1 = openType=='2'?'2':'1';
        var arg2 = dateType=='1'?'1':'0';
        var arg3='';
        if(dataSelect){
            arg3 = typeof dataSelect == 'string'?dataSelect:JSON.stringify(dataSelect);
        }
        uexCalendar.open(arg1,arg2,arg3);
    }

    /**
     * 根据窗口名称关闭窗口
     * @param {string|array} winName
     * @param {[number]} aniId
     * @param {[number]} duration
     */
    function closeWindowByName(winName, aniId, duration){
        var winNames;

        if(winName instanceof Array){
            winNames = winName
        }else{
            winNames = [winName];
        }

        winNames.forEach(function (nm) {
            evalScriptInWindow(nm, 'uexWindow.close()', 0);
        })
    }

    /**
     * evalScriptInWindow
     * @param {string} winName
     * @param {string} scriptStr
     * @param {[number]} delay
     */
    function evalScriptInWindow(winName, scriptStr, delay){
        setTimeout(function () {
            uexWindow.evaluateScript(winName, 0, scriptStr);
        }, delay||0);
    }

    /**
     * 查看图片
     * @param imageUrlSet {Array} 要预览的图片列表
     * @param activeIndex {number} 打开到的索引值
     */
    function openImageViewer(imageUrlSet, activeIndex){
        var paramObj = {
            displayActionButton: false,
            displayNavArrows: false,
            enableGrid: false,
            startOnGrid: false,
            startIndex: activeIndex || 0,
            data: imageUrlSet
        };
        var paramStr = JSON.stringify(paramObj);
        uexImage.openBrowser(paramStr);
    }

    /**
     * 选择图片
     * @param cb 回调函数
     * @param options 选项
     */
    function openImagePicker(cb, options){
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
    }

    //EXPORT
    w.qlib = {};

    qlib.j2s = j2s;
    qlib.s2j = s2j;
    qlib.j2ls = json2localStorage;
    qlib.ls2j = localStorage2json;
    qlib.uexCalendar = selectDate;
    qlib.closeWindowByName = closeWindowByName;
    qlib.evalScriptInWindow = evalScriptInWindow;
    qlib.openImageViewer = openImageViewer;
    qlib.openImagePicker = openImagePicker;
})(window);