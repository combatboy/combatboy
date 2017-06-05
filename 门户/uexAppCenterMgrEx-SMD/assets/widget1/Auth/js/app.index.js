/**
 * Created by mitty on 15/12/1.
 */

//绑定手势登录成功回调
function bindLockPatternLoginSuccessHandler(){
    uexLockPattern.cbLoginSuccess = function (opId, dataType, data) {
        localStorage.setItem('simcere.runtime.loginLock', '1');
        IMLogin.call();
        if (data == '0') {
            // unlock ok
            //alert('auth:unlock ok');
        } else if (data == '1') {
            // modify ok
            //alert('auth:modify ok');
            // todo: HACK uexLockPattern.init 触发了这里，不知道什么原因
            // 需要主动关闭手势界面
            localStorage.removeItem('simcere.runtime.isScreenLocked');
            uexLockPattern.close();
            // 保存手势状态
            localStorage.setItem('simcere.runtime.hasGesturePassword', '1');
            // 重定向
            appcan.window.open("Index_index", "../../Index/html/index.html", 0);
        } else if (data == '2') {
            //alert('auth:init ok');
            // 需要主动关闭手势界面
            localStorage.removeItem('simcere.runtime.isScreenLocked');
            uexLockPattern.close();
            // 保存手势状态
            localStorage.setItem('simcere.runtime.hasGesturePassword', '1');
            // 重定向
            appcan.window.open("Index_index", "../../Index/html/index.html", 0);
        }
    };
}

/**
 * EMM登录
 */
function emmLoginHandler(inLoginId, inLoginPassword, isLockPatternChecked){
    var proccessCounter = 0;
    var proccessSteps = 2;
    function syncProccessWatcher(sourceCode){
        proccessCounter++;
        //alert(sourceCode+'->'+proccessCounter);
        if(proccessCounter == proccessSteps){
            //todo: hack 刷新首页
            appcan.window.publish('Auth/index.loginSuccess', '');
            //手势
            if (!isLockPatternChecked) {
                IMLogin.call(); 
                appcan.window.open("Index_index", "../../Index/html/index.html", 10);
            } else {
                /**
                 * 初始化手势密码
                 * @param 维度
                 * @param 重试次数限制
                 * @param 未知
                 */
                uexLockPattern.init('3', '5', '0');
            }
        }
    }
    var loginInfoObj = {
                "loginName": inLoginId,
                "loginPass": inLoginPassword,
                "domainName": SimcereConfig.emmLoginDomain
            },
            loginInfoStr = JSON.stringify(loginInfoObj);

    //绑定uexEMM登录回调
    uexEMM.cbLogin = function (a, b, json){
        appcan.window.closeToast();
        var loginCbInfoObj;
        //解析uexEMM登录返回的数据
        try{
            loginCbInfoObj = JSON.parse(json);
        }catch(ex){
            appcan.window.openToast('EMM参数解析失败',2000);
        }

        if(loginCbInfoObj.status!='ok'){
            //todo: 认证失败，需要显示中文提示
            console.log(json);
            appcan.window.alert('提示', loginCbInfoObj.info);
        }else{
            //EMM登录认证通过
            //alert('EMM登录认证通过');

            //缓存EMM登录信息
            //登录时填写的用户id
            //todo: 需要加密
            localStorage.setItem('simcere.runtime.loginId', loginInfoObj.loginName);
            localStorage.setItem('simcere.runtime.loginPw', loginInfoObj.loginPass);
            //甲方的userId（数据库中为ID字段）
            localStorage.setItem('simcere.runtime.uniqueField', loginCbInfoObj.info.uniqueField);
            //甲方的userId（数据库中为ID字段）
            //emm返回的userId为emm与甲方对接时产生的id，app前端暂未使用
            localStorage.setItem('simcere.runtime.userId', loginCbInfoObj.info.uniqueField);
            //用户名，如‘张三’
            localStorage.setItem('simcere.runtime.userName', loginCbInfoObj.info.username);
            //im用户登陆id（数据库中为mobileUserId字段）
            localStorage.setItem('simcere.runtime.imUserId', loginCbInfoObj.info.mobileUserId);
            //
            localStorage.setItem('simcere.runtime.domainId', loginCbInfoObj.info.domainId);

            //同时登录bbs
            (function(){
                var s = [
                    'mod=logging',
                    'action=login',
                    'username=' + inLoginId,
                    'password=' + inLoginPassword
                ].join('&');
                var verifyUrl = SimcereConfig.server.bbs + 'member.php?' + s;
                var verifyInfo = zkutil.getAppVerifyInfo();
                appcan.window.openToast('子应用认证');
                appcan.request.ajax({
                    type: 'GET',
                    url: verifyUrl,
                    data: '',
                    contentType: 'application/json',
                    dataType: 'text',
                    appVerify: false,
                    beforeSend: function(xhr){
                        //alert(JSON.stringify(verifyInfo));
                        xhr.setRequestHeader('x-mas-app-id', verifyInfo.appId);

                        xhr.setRequestHeader('appId', verifyInfo.appId);
                        xhr.setRequestHeader('appkey', verifyInfo.appKey);
                        xhr.setRequestHeader('timestamp', verifyInfo.timestamp);
                        xhr.setRequestHeader('appverify', verifyInfo.appverify);
                    },
                    success: function (data, status, xhr) {
                        appcan.window.closeToast();
                        var dataJsonStr = data.substr(1, data.length - 2);
                        var dataJson = JSON.parse(dataJsonStr);
                        var bbsAuthObj = {};
                        var bbsAuthStr;

                        //登录bbs失败
                        if (dataJson.status != 'LoginSuccess') {
                            appcan.window.alert('提示', 'BBS同步认证失败');
                        }else{
                            //将用户信息保存到bbs的MySQL数据库中
                            console.log('将用户信息保存到bbs的MySQL数据库中:'+loginCbInfoObj.info.uniqueField);
                            saveUserInfoToBBS(loginCbInfoObj.info.uniqueField);

                            //按照bbs中的要求保存到本地存储
                            bbsAuthObj[dataJson.uid] = {
                                "uid": dataJson.uid,
                                "uname": inLoginId,
                                "upwd": inLoginPassword
                            };
                            bbsAuthStr = JSON.stringify(bbsAuthObj);

                        //todo: 不规范的命名，因为借用bbs模块，所以暂不修正
                        localStorage.setItem('UID', dataJson.uid);
                        localStorage.setItem('loginObj', bbsAuthStr);
                        localStorage.setItem('showimage'+dataJson.uid, true);

                            console.log('bbs登录成功，UID: '+localStorage.getItem('UID'));
                            console.log(bbsAuthStr);
                            console.log('cookie: '+document.cookie);
                            console.log('domain: '+document.domain);

                            setTimeout(function () {
                                syncProccessWatcher(50002);
                            }, 200);
                        }

                    },
                    error: function (xhr, errorType, error) {
                        //
                        appcan.window.closeToast();
                        appcan.window.alert('提示', 'BBS同步认证失败：网络连接不可用');
                    }
                });

                //debug
                //bbsLoginDebugger(verifyInfo);
            })();

            /**
             * 获取硬件信息并执行回调：
             * 1.激活session
             * 2.设置推送用户信息
             * 3.softToken
             *
             * 回调参数：
             * @param {object} widgetInfoObj
             */
            zkutil.getCurrentWidgetInfo.call(null, function (widgetInfoObj) {
//alert(JSON.stringify(widgetInfoObj));

                /**
                 * 设置推送用户信息
                 * @param uId 用户ID
                 * @param uNickName 用户昵称
                 */
//alert('设置推送用户信息' + loginCbInfoObj.info.userId + ':' +loginCbInfoObj.info.uniqueField);
                uexWidget.setPushInfo(loginCbInfoObj.info.userId, loginCbInfoObj.info.uniqueField);

                /**
                 * softToken
                 * @param a
                 * @param b
                 * @param c
                 */
                uexEMM.cbGetSoftToken = function (a, b, c) {
                    //todo: softToken
//                        alert('softToken: ' + c);
                };
                uexEMM.getSoftToken();

                //获取激活session需要请求的url
                var sessionAuthUrl = zkutil.getSessionInitUrl(widgetInfoObj.appId, loginCbInfoObj.info.accessToken);
//alert('sessionAuthUrl: ' + sessionAuthUrl);
                //激活session
                $.getJSON(sessionAuthUrl, function (data) {
                    //do nothing, like cttq
//alert('$.getJSON1: ' + JSON.stringify(data));
                    syncProccessWatcher(50001);
                }, 'json', function (e) {
                    //do nothing, like cttq
//alert('do nothing, like cttq 2');
                    appcan.window.alert('提示', '建立会话失败');
                }, 'GET', '', '');
            });
        }

    };
    //执行登录
//alert(loginInfoStr);
    appcan.window.openToast('正在登录');
    uexEMM.login(loginInfoStr);

}
/**
 * 用户登陆后进行IM登陆
 * @constructor
 */
function IMLogin(){
    var imUserId = localStorage.getItem('simcere.runtime.imUserId');
    var imLoginInfo = {
        imId : imUserId ,
        imPassword : "123456"
    }
    appcan.window.publish("login",JSON.stringify(imLoginInfo));
}
/**
 * 获取emm端所有用户信息userId -- userName
 */
function getUser(){
    var userId_name=localStorage.getItem("simcere.im.userId_name");
    if(userId_name == null || userId_name==""){
        userId_name ={};
    }else{
       userId_name = JSON.parse(localStorage.getItem("simcere.im.userId_name"));
    }
    var url = SimcereConfig.server.mas+'/appIn/queryUser/all';   
    appcan.request.ajax({
        url : url,
        type : 'GET',
        dataType : "text" ,
        success : function(data) {
            var data=JSON.parse(data);
            for(var i=0;i<data.length;i++){
                userId_name[data[i].userId]={
                    userName : data[i].userName
                }
            }
        localStorage.setItem("simcere.im.userId_name",JSON.stringify(userId_name));
        }
    })
}
function bbsLoginDebugger(verifyInfo){
    //todo: debug, send to log server
    appcan.request.ajax({
        type: 'GET',
        url: 'http://192.168.1.105:50002/',
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
        success: function (data, status, xhr) {},
        error: function (xhr, errorType, error) {}
    });
}
/**
 * 更新bbs中的头像
 * @param photoUrl
 */
function updateAvatarInBBS(photoUrl) {
    var currentLoginId = localStorage.getItem('simcere.runtime.loginId');
    var params = {
        "username": currentLoginId,
        "avatar": photoUrl
    };
    console.log('updateAvatarInBBS:');
    console.log(JSON.stringify(params));
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
}
/**
 * 将用户信息保存到bbs的MySQL数据库中（步骤二）
 * @param realname
 * @param deptName
 * @param deptId
 */
function saveUserInfoToBBSHelper(realname, deptName, deptId){
    var s = [
        'home.php?mod=spacecp&ac=profile&op=editprofile',
        'realname=' + realname,
        'deptName=' + deptName,
        'deptId=' + deptId
    ].join('&');
    var params = {};
    appcan.request.ajax({
        type: 'POST',
        url: SimcereConfig.server.bbs + s,
        data: params,
        contentType: 'application/json',
        dataType: 'text',
        success: function (data, status, xhr) {
            var jo = eval(data);
            if (jo.status != 'ok') {
                appcan.window.openToast('BBS用户信息同步失败',2000);
            }else{
                console.log('saveUserInfoToBBSHelper ok.');
            }
        },
        error: function (xhr, errorType, error) {
            appcan.window.openToast('BBS用户信息同步失败：网络连接不可用',3000);
            console.error('saveUserInfoToBBSHelper failed: '+error);
        }
    });
}
/**
 * 将用户信息保存到bbs的MySQL数据库中（步骤一）
 * @param userId
 */
function saveUserInfoToBBS(userId){
    var params = {
        "userId": userId
    };
    appcan.window.openToast('同步用户信息');
    appcan.request.ajax({
        type: 'POST',
        url: SimcereConfig.server.mas + '/contact/ScrQueryUserDetail',
        data: params,
        contentType: 'application/json',
        dataType: 'json',
        success: function (data, status, xhr) {
            appcan.window.closeToast();
            if(data.satus=='0'||data.status=='0'){
                var u = data.user||{};
                saveUserInfoToBBSHelper(u.LASTNAME, u.DEPT);
                //如果有头像，同步更新bbs
                if(data.photo && data.photo.photo){
                    console.log('同步头像...');
                    updateAvatarInBBS(data.photo.photo);
                }
            }else{
                appcan.window.openToast('无法获取用户信息',2000);
                console.error('fail: 将用户信息保存到bbs的MySQL数据库中');
            }
        },
        error: function (xhr, errorType, error) {
            appcan.window.closeToast();
            appcan.window.alert('提示', '无法获取用户信息：网络连接不可用');
        }
    });
}