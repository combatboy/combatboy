/**
 * 缓存类
 * Dependents:
 *  1.QNS
 * Exports:
 *  1.QNS.Cache
 * Created by mitty on 15/9/2.
 */
(function () {
    /**
     * 请求缓存构建函数
     * @param moduleName
     * @param isEncrypt
     * @constructor
     */
    function Cache(moduleName, isEncrypt){
        var typeof_keyPrefix = typeof moduleName,
            typeof_isEncrypt = typeof isEncrypt;
        if(typeof_keyPrefix!='string'){
            throw new TypeError('expected parameter "keyPrefix" to be string,' + typeof_keyPrefix + ' provided');
        }

        this.keyPrefix = 'Cache_' + moduleName;
        this.isEncrype = isEncrypt;
    }

    /**
     * 生成缓存键名
     * @param url
     * @param param
     * @returns {string}
     * @private
     */
    Cache.prototype._getCacheKey = function(url, param){
        var self = this;
        //check arguments
        var typeof_url = typeof url,
            typeof_param = typeof param;
        if (typeof_url != 'string') {//allow empty url
            throw new TypeError('expected parameter "url" to be string,' + typeof_url + ' provided');
        } else if (typeof_param != 'object') {
            throw new TypeError('expected parameter "param" to be object,' + typeof_param + ' provided');
        }

        //构建类uri的缓存键名
        var paramHeap = [];
        for (var k in param) {
            if (param.hasOwnProperty(k)) {
                paramHeap.push(encodeURIComponent(k) + '=' + encodeURIComponent(param[k]));
            }
        }

        //排序，防止参数顺序影响结果
        paramHeap.sort(function (a, b) {
            return a.split('=')[0] < b.split('=')[0] ? -1 : 1;
        });

        if (paramHeap.length) {
            return self.keyPrefix + '_' + url + '?' + paramHeap.join('&');
        } else {
            return self.keyPrefix + '_' + url;
        }
    };

    /**
     * Getter
     * @param url
     * @param param
     * @returns {*}
     */
    Cache.prototype.get = function (url, param) {
        var k = this._getCacheKey(url, param);
        return localStorage.getItem(k);
    };

    /**
     * Setter
     * @param {string} url 请求的url
     * @param {object} param 请求的参数
     * @param {string} data
     * @returns {*}
     */
    Cache.prototype.set = function (url, param, data) {
        //check arguments
        var typeof_data = typeof data;
        if(typeof_data != 'string'){
            console.error('expected parameter "url" to be string,' + typeof_data + ' provided');
            return false;
        }

        var cacheKey = this._getCacheKey(url, param);
        try{
            localStorage.setItem(cacheKey,data);
        }catch(ex){
            console.error(ex.message);
            return false;
        }
        return cacheKey;
    };

    //EXPORT
    QNS.Cache = Cache;
})();
