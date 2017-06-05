/**
 * 请求类
 * Dependents:
 *  1.Zepto.js||jQuery.js
 *  2.QNS.util.genOpId
 *  3.QNS.Cache
 * Exports:
 *  1.QNS.Request
 *  2.QNS.request
 * Created by mitty on 15/8/31.
 */
(function (qns) {
    /**
     * 请求构建函数
     * @param url
     * @param method
     * @constructor
     */
    function Request (url, method) {
        var typeofUrl = typeof url,
            methods = ['get','post',undefined];

        //check params
        if (typeofUrl != 'string') {
            throw new TypeError('expected parameter "url" to be string,' + typeofUrl + ' provided');
        }else if(methods.indexOf(method) == -1){
            throw new RangeError('provided parameter value for "method" is not allowed');
        }

        //opt
        var opt = { url: url, method: method || 'post'};
        //必须进行deep clone
        this.options = $.extend(JSON.parse(JSON.stringify(Request.defaults)), opt);
        //cache
        this.cache = new QNS.Cache('Contacts', false);
        //callbacks
        this.callbacks = { beforeSend: [], download: [], success: [], error: [], complete: [], upload: []};
    }
    Request.init = function () {
        if(!uexXmlHttpMgr){
            throw new ReferenceError('uexXmlHttpMgr is not defined');
        }else if(Request.isInit){
            console.warn('Request already init');
        }else{
            uexXmlHttpMgr.onData = function (id,status,result,requestCode,response) {
                var evtName = 'qns:request.ondata';
                var evt = $.Event(evtName, { bubbles: false });
                $(document).triggerHandler(evt, [id, status,result,requestCode,response]);
            };
            uexXmlHttpMgr.onPostProgress = function (id, progress) {
                var evtName = 'qns:request.onpostprogress';
                var evt = $.Event(evtName, { bubbles: false });
                $(document).triggerHandler(evt, [id, progress]);
            };
            Request.isInit = true;
            console.log('Request init ok');
        }
    };

    /**
     * Defaults
     * @type {{plain object}}
     */
    Request.defaults = {
        //hasCache: false,

        conventers: [],
        data: {},
        headers: {
            "Content-Type": "application/json",
            "contentType": "",
            "dataType": "json"//xml, json, script, or html
        },
        cache: false,
        timeout: 30 * 1000
    };

    Request.httpStatusCode = {
        '100':'Continue',
        '101':'Switching Protocols',
        '102':'Processing',

        '200':'OK',
        '201':'Created',
        '202':'Accepted',
        '203':'Non-Authoritative Information',
        '204':'No Content',
        '205':'Reset Content',
        '206':'Partial Content',
        '207':'Multi-Status',

        '300':'Multiple Choices',
        '301':'Moved Permanently',
        '302':'Found',
        '303':'See Other',
        '304':'Not Modified',
        '305':'Use Proxy',
        '306':'Switch Proxy',
        '307':'Temporary Redirect',

        '400':'Bad Request',
        '401':'Unauthorized',
        '402':'Payment Required',
        '403':'Forbidden',
        '404':'Not Found',
        '405':'Method Not Allowed',
        '406':'Not Acceptable',
        '407':'Proxy Authentication Required',
        '408':'Request Timeout',
        '409':'Conflict',
        '410':'Gone',
        '411':'Length Required',
        '412':'Precondition Failed',
        '413':'Request Entity Too Large',
        '414':'Request-URI Too Long',
        '415':'Unsupported Media Type',
        '416':'Requested Range Not Satisfiable',
        '417':'Expectation Failed',
        '418':'I\'m a teapot',
        '421':'There are too many connections from your internet address',
        '422':'Unprocessable Entity',
        '423':'Locked',
        '424':'Failed Dependency',
        '425':'Unordered Collection',
        '426':'Upgrade Required',
        '449':'Retry With',

        '500':'Internal Server Error',
        '501':'Not Implemented',
        '502':'Bad Gateway',
        '503':'Service Unavailable',
        '504':'Gateway Timeout',
        '505':'HTTP Version Not Supported',
        '506':'Variant Also Negotiates',
        '507':'Insufficient Storage',
        '509':'Bandwidth Limit Exceeded',
        '510':'Not Extended'
    };

    /**
     * 设置、修改选项
     * @param key
     * @param val
     * @returns {Request}
     */
    Request.prototype.setOption = function (key, val) {
        this.options[key] = val;
        return this;
    };

    /**
     * 设置、修改选项
     * @param obj
     * @returns {Request}
     */
    Request.prototype.setOptions = function (obj) {
        $.extend(this.options, obj);
        return this;
    };

    /**
     * 设置、修改headers
     * @param key
     * @param val
     * @returns {Request}
     */
    Request.prototype.setHeader = function (key, val) {
        this.options.headers[key] = val;
        return this;
    };

    /**
     * 设置、修改headers
     * @param obj
     * @returns {Request}
     */
    Request.prototype.setHeaders = function (obj) {
        $.extend(this.options.headers, obj);
        return this;
    };

    /**
     * 请求回调
     * @param type
     * @param func
     * @returns {Request}
     */
    Request.prototype.on = function (type, func) {
        if(!this.callbacks.hasOwnProperty(type)){
            throw new RangeError('provided parameter value for "type" is not allowed');
        }
        this.callbacks[type].push(func);
        return this;
    };
    /**
     * 调用send方法前的回调
     * @param func
     * @param xhr
     * @param settings
     * @returns {Request}
     */
    Request.prototype.beforeSend = function (func) {
        return this.on('beforeSend',func);
    };
    /**
     * 下载中回调
     * @param func
     * @returns {Request}
     */
    Request.prototype.download = function (func) {
        return this.on('download',func);
    };
    /**
     * 上载中回调
     * @param func
     * @returns {Request}
     */
    Request.prototype.upload = function (func) {
        return this.on('upload',func);
    };
    /**
     * 请求成功回调
     * @param func
     * @param data
     * @param statusCode
     * @param xhr
     * @param options
     * @returns {Request}
     */
    Request.prototype.success = function (func) {
        return this.on('success',func);
    };
    /**
     * 请求失败回调
     * @param func
     * @param xhr
     * @param statusCode
     * @param error
     * @param result
     * @returns {Request}
     */
    Request.prototype.error = function (func) {
        return this.on('error',func);
    };
    /**
     * 请求结束回调
     * @param func
     * @param xhr
     * @param statusCode
     * @returns {Request}
     */
    Request.prototype.complete = function (func) {
        return this.on('complete',func);
    };
    /**
     * wgt.打开一个跨域请求连接
     * @private
     */
    Request.prototype._xhrOpen = function () {
        var self = this,
            opt = this.options;
        this._opid = qns.util.genOpId();
        this._xhr.open(self._opid, opt.method, opt.url, opt.timeout);
    };
    /**
     * wgt.关闭一个跨域请求连接
     */
    Request.prototype.close = function () {
        this._xhr.close(this._opid);
    };
    /**
     * wgt.绑定回调函数
     * @private
     */
    Request.prototype._xhrBind = function () {
        var self = this;
        /**
         * 每个Request对象会监听与其opid对应的请求响应事件
         * @param e event object
         * @param id opid
         * @param status 0:receive,1:finish,-1:error
         * @param result returned data,even error
         * @param requestCode 0:text,1:binary
         * @param response 服务器返回的信息，该字符串为JSON格式
         */
        $(document).on('qns:request.ondata', function (e, id, status,result,requestCode,response) {
            if(id!=self._opid){
                return;
            }
            var data,
                parseMsg = '',
                isJson = self.options.headers.dataType=='json',
                isParseError = false;

            if(isJson){
                try{
                    data = JSON.parse(result);
                }catch(ex){
                    isParseError = true;
                    parseMsg = ex.message;
                }
            }

            if(status==0){//receive
                self.callbacks.download.forEach(function (f) {
                    f.call(null);
                });
            }else if(status==1 && !isParseError){//finish
                //检查缓存
                if(self.options.cache){
                    var cacheData = self.cache.get(self.options.url, self.options.data);
                    if(cacheData == result){
                        console.info('cached data is up-to-date');
                        return;
                    }
                }
                self.callbacks.success.forEach(function (f) {
                    f.call(null, isJson ? data : result, requestCode, self._xhr, self.options);
                });
                //cache
                if(self.options.cache){
                    self.cache.set(self.options.url, self.options.data, result);
                }
            }else if(status==-1 || isParseError){//error
                self.callbacks.error.forEach(function (f) {
                    f.call(null, self._xhr, requestCode, {}, result);
                });
            }
        }).on('qns:request.upload.'+self._opid, function (e, id, progress) {
            if(id!=self._opid){
                return;
            }
            self.callbacks.upload.forEach(function (f) {
                f.call(null, progress);
            });
        });
    };
    /**
     * wgt.底层发送请求函数
     * @param flag
     * @private
     */
    Request.prototype._xhrSend = function (flag) {
        var self = this,
            opt = self.options,
            headers = opt.headers,
            headersStr = JSON.stringify(headers),
            data = opt.data,
            dataStr = $.isPlainObject(data) ? JSON.stringify(data) : data;

        //设置请求头
        this._xhr.setHeaders(this._opid, headersStr);

        //post data
        /*for (var key in data) {
            if (data.hasOwnProperty(key)) {
                self._xhr.setPostData(self._opid, 0, key, (typeof data[key]=='object'?JSON.stringify(data[key]):data[key]));
            }
        }*/

        //request body
        this._xhr.setBody(this._opid, dataStr);

        //todo:setInputStream

        //调用底层发送方法
        this._xhr.send(this._opid, flag);
    };
    /**
     * 发送请求
     */
    Request.prototype.send = function (flag) {
        var self = this,
            opt = self.options,
            data = opt.data;

        //初始化底层请求对象
        if(!uexXmlHttpMgr){
            throw new ReferenceError('uexXmlHttpMgr is not defined');
        }else if(!self._xhr){
            if(!Request.isInit){
                Request.init();
            }
            self._xhr = uexXmlHttpMgr;
            self._xhrOpen();
            self._xhrBind();
        }

        //分页
        if(data.pageNo && data.pageSize){
            self.pageHydrator();
        }

        //读缓存
        if(opt.cache){
            var cacheData = self.cache.get(opt.url, opt.data);
            if(cacheData){
                var cacheDataParse = opt.headers.dataType == 'json' ? JSON.parse(cacheData) : cacheData;
                console.info('cache hit');
                opt.hasCache = true;
                self.callbacks.success.forEach(function (f) {
                    f.call(null, cacheDataParse, '314', self._xhr, self.options);
                });
            }
        }

        //发送
        this._xhrSend(1);
    };
    /**
     * 下一页
     */
    Request.prototype.pageNext = function () {
        this._checkPage();
        var data = this.options.data;
        data.pageNo++;
        this.send(1);
    };
    /**
     * 上一页
     */
    Request.prototype.pagePrev = function () {
        this._checkPage();
        var data = this.options.data;
        if(data.pageNo<=1){
            console.warn('pageNo underflow');
            return;
        }
        data.pageNo = Math.max(data.pageNo-1, 1);
        this.send(1);
    };
    /**
     * 检查分页参数
     * @private
     */
    Request.prototype._checkPage = function () {
        var data = this.options.data;
        if(!data.pageNo){
            throw new ReferenceError('pageNo is not defined');
        }else if(!data.pageSize){
            throw new ReferenceError('pageSize is not defined');
        }else if(typeof data.pageNo!='number' || data.pageNo<1){
            throw new RangeError('pageNo should be a positive integer')
        }else if(typeof data.pageNo!='number' || data.pageSize<1){
            throw new RangeError('pageSize should be a positive integer')
        }
    };

    /**
     * 由于各个服务器的分页参数名不尽相同，所以需要提供转换方法
     * @param a pageNo
     * @param b pageSize
     */
    Request.prototype.pageHydrator = function (a, b) {
        var self = this,
            opt = self.options,
            data = opt.data;
        data[a || 'pageNumber'] = data['pageNo'];
        data[b || 'pageSize'] = data['pageSize'];
    };

    /**
     * 提供Request的快速调用方式
     * @param url
     * @param method
     * @returns {Request}
     */
    function request(url, method) {
        return new Request(url, method);
    }

    //EXPORT
    qns.Request = Request;
    qns.request = request;
})(QNS);
