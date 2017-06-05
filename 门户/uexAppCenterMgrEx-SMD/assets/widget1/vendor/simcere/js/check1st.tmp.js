/**
 * Created by mitty on 16/2/18.
 */
appcan.ready(function () {
    //迭代版本够多，不需要此信息了，否则全新安装的app还会提示此消息
    return;
    function formatVerNo(s){
        return s.split('.').join('');
    }
    if (!Zepto.os.ios) {
        console.log('not ios');
        return;
    }

    //检查首次登录
    var stoKey = 'Simcere.isFirstLogin';
    //localStorage.removeItem(stoKey);

    var is1stLogin = localStorage.getItem(stoKey) != 'n';
    if (is1stLogin) {
        zkutil.getCurrentWidgetInfo.call(null, function(o) {
            //更换评估证书后的第一个版本号
            var iosCerVerNo = '01.01.0002';
            if (formatVerNo(o.version) < formatVerNo(iosCerVerNo)) {
        console.log('first');
        appcan.window.alert('提示', '恭喜您已经成功升级为最新版本，如手机中出现两个应用图标，请删除右上角不带“New”图标的程序，给您造成的不便还请谅解。');
        localStorage.setItem(stoKey, 'n');
            }
        });
    }else{
        console.log('not first');
    }
});