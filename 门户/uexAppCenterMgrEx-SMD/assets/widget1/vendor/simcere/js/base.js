/**
 * 常用脚本
 *
 * 1.绑定了需要全局监听的事件回调
 * 2.封装了一些常用的方法，namespace为zkutil
 *
 * Created by mitty on 15/11/25.
 */
$(function () {
    $('#tabview').on('tap', 'a', function () {
        //监听底部导航tap事件
        var $this = $(this),index = $this.data('index');
        // if(index=='0'){
            // appcan.window.openToast('功能开发中', 2000);
            // return;
        // }
        var mapWin = [{
            name: 'IM_index',
            url: '../../IM/html/index.html'
        }, {
            name: 'Index_index',
            url: '../../Index/html/index.html'
        }, {
            name: 'Home_index',
            url: '../../Home/html/indexTop.html'
        }, {
            name: 'Agency_index',
            url: '../../daiban/html/daiban.html'
        }];
        if($this.children().is('.sc-text-active')){
            console.log('当前窗口');
        }else{
            appcan.window.open({
                name: mapWin[index].name,
                data: mapWin[index].url,
                aniId: 0
            });
        }
    });
});


(function(w,d,de,b){
    /**
     * 设置当前页面是否拦截某个按键（Android平台专用）
     *
     * @param keyCode 要拦截的键值
     * 0:返回
     * 1:菜单
     * @param isIntercept 是否拦截
     * 0:不拦截
     * 1:拦截
     * @param callback 回调
     * data: keyCode
     * eg. uexWidgetOne.exit();
     */
    function setReportKey(keyCode, isIntercept, callback) {
        var plat = uexWidgetOne.getPlatform();
        if(plat){
            uexWindow.onKeyPressed=callback;
            uexWindow.setReportKey(keyCode, isIntercept);
        }
    }

    /**
     * 获取当前登录用户信息
     *
     * @returns {{id: *, name: *}}
     */
    function getCurrUser() {
        return {
            id: localStorage.getItem('simcere.runtime.userId'),
            name: localStorage.getItem('simcere.runtime.userName')
        };
    }

    /**
     * 获取当前应用信息
     * @param {function} callback
     */
    function getCurrentWidgetInfo(callback) {
        /**
         * 获取当前应用信息
         * @param a
         * @param b
         * @param {string} dataStr
         *  dataStr.name
         *  dataStr.appId
         *  dataStr.icon
         *  dataStr.version
         */
        uexWidgetOne.cbGetCurrentWidgetInfo = function (a, b, dataStr) {
            var dataObj = JSON.parse(dataStr);
            localStorage.setItem('simcere.runtime.appId', dataObj.appId);
            //setLocVal('simcere.runtime.appVersion', obj.version);
            //执行回调
            callback.call(null, dataObj);
        };
        uexWidgetOne.getCurrentWidgetInfo();
    }

    /**
     * 获取激活mas session的请求url
     * @param appId
     * @param accessToken
     * @returns {string}
     */
    function getSessionInitUrl(appId, accessToken) {
        return [
            SimcereConfig.server.mas,
            '/',
            appId,
            '/',
            accessToken,
            '/base/activeSession'
        ].join('');
    }

    /**
     * 根据窗口名称关闭窗口
     * @param {string|array} winName
     * @param {[number]} aniId
     * @param {[number]} duration
     */
    function closeWindowByName(winName, aniId, duration) {
        var winNames;

        if (winName instanceof Array) {
            winNames = winName
        } else {
            winNames = [winName];
        }

        winNames.forEach(function (nm) {
            evalScriptInWindow(nm, 'uexWindow.close()', 0);
        });
    }

    /**
     * evalScriptInWindow
     * @param {string} winName
     * @param {string} scriptStr
     * @param {[number]} delay
     */
    function evalScriptInWindow(winName, scriptStr, delay) {
        setTimeout(function () {
            uexWindow.evaluateScript(winName, '0', scriptStr);
        }, delay || 0);
    }

    /**
     * 获取app验证码，用于bbs静默登录
     * @returns {*}
     */
    function getAppVerifyInfo () {
        var ts=new Date().getTime().toString();
        var k = [
            SimcereConfig.appId,
            SimcereConfig.appKey,
            ts
        ].join(':');
        return {
            "appId": SimcereConfig.appId,
            "appKey": SimcereConfig.appKey,
            "timestamp": ts,
            "appverify": 'md5='+appcan.crypto.md5(k).toLowerCase()+';ts='+ts
        };
    }

    //同时登录bbs
    function bbsLoginHandler(username, password){
        var inLoginId = username || localStorage.getItem('simcere.runtime.loginId') || '',
                inLoginPassword = password || localStorage.getItem('simcere.runtime.loginPw') || '';

        console.log('Extend the life cycle of bbs cookie '+inLoginId+':'+inLoginPassword);

        var s = [
            'mod=logging',
            'action=login',
            'username=' + inLoginId,
            'password=' + inLoginPassword
        ].join('&');
        var verifyUrl = SimcereConfig.server.bbs + 'member.php?' + s;
        var verifyInfo = getAppVerifyInfo();
        setTimeout(function () {
            var img = new Image();
            console.log('请求延长会话');
            img.src = SimcereConfig.server.mas + '/base/test?loginid=' + inLoginId;
        }, 0);
        appcan.request.ajax({
            type: 'GET',
            url: verifyUrl,
            data: '',
            contentType: 'application/json',
            dataType: 'text',
            appVerify: false,
            beforeSend: function(xhr){
                xhr.setRequestHeader('x-mas-app-id', verifyInfo.appId);
                xhr.setRequestHeader('appId', verifyInfo.appId);
                xhr.setRequestHeader('appkey', verifyInfo.appKey);
                xhr.setRequestHeader('timestamp', verifyInfo.timestamp);
                xhr.setRequestHeader('appverify', verifyInfo.appverify);
            },
            success: function (data, status, xhr) {
                var dataJsonStr = data.substr(1, data.length - 2);
                try{
                    var dataJson = JSON.parse(dataJsonStr);
                }catch(ex){
                    console.error(ex.message);
                }
                //登录bbs失败
                if (dataJson.status != 'LoginSuccess') {
                    console.error('Fail to Extend the life cycle of bbs cookie.');
                }else{
                    console.info('Extend the life cycle of bbs cookie ok.');
                }
            },
            error: function (xhr, errorType, error) {
                console.error('Fail to Extend the life cycle of bbs cookie.');
            }
        });
    }

    //EXPORT
    w.zkutil = {
        setReportKey: setReportKey,
        getCurrUser: getCurrUser,
        getCurrentWidgetInfo: getCurrentWidgetInfo,
        getSessionInitUrl: getSessionInitUrl,
        closeWindowByName: closeWindowByName,
        evalScriptInWindow: evalScriptInWindow,
        getAppVerifyInfo: getAppVerifyInfo,
        bbsLoginHandler: bbsLoginHandler
    };
})(window,document,document.documentElement,document.body);


function logs(m){
    uexLog.sendLog(m);
}