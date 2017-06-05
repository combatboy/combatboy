/**
 * Created by mitty on 15/9/7.
 */
var pageutil = {};
pageutil.filterClosestDepartment = function (a) {
    var res = [];
    for (var i = 1; i < 5; i++) {
        if (/^0+$/.test(a['ZZ_JG' + i])==false) {
            res.push({
                id: a['ZZ_JG' + i],
                name: a['ZZ_JG' + i + 'T']
            });
        }
    }
    return res.slice(-2);
};

pageutil.openContactDetail = function (personIdView) {
    var typeof_personIdView = typeof personIdView;

    //参数检查
    if(typeof_personIdView!='string'){
        throw new TypeError('expected parameter "psnId" to be string,' + typeof_personIdView + ' provided')
    }else if(personIdView.length==0){
        throw new TypeError('"psnId" can not be a empty string');
    }

    //缓存要查看的人员id，打开详情
    localStorage.setItem('Contacts/index.personIdView', personIdView);
    var htmlFileLocation = 'contactDetail.html';
    appcan.window.open({
        name: 'Contacts_contactDetail',
        data: htmlFileLocation,
        aniId: 10
    });
};

pageutil.scrollToTop = (function() {
    /**
     * 回到顶部
     * @param {number} duration
     * @param {number} frame
     */
    var isScrolling = false;
    return function (duration, frame) {
        if(isScrolling===false){
            isScrolling = true;
        }else{
            console.warn('scrollToTop: in prog');
            return;
        }

        var $body = $(document.body),
            scrollTop = Math.abs($body.scrollTop()),
            t = duration || 400,
            f = frame || 60,//每秒帧数
            d = t / f,
            deltaMove = parseInt(scrollTop / 25, 10),
            last;

        var elm = document.getElementById('dbg');

        var intervalId = setInterval(function () {
            scrollTop -= Math.ceil(deltaMove);
            if (scrollTop > 0 && scrollTop!=last) {
                last = scrollTop;
                $body.scrollTop(scrollTop);
            } else {
                $body.scrollTop(0);
                clearInterval(intervalId);
                isScrolling = false;
            }
        }, d);
    }
})();