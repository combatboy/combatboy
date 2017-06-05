/**
 * QNS名字空间
 * Dependents:
 *  null
 * Exports:
 *  1.QNS.Cache
 * Created by mitty on 15/8/9.
 */

(function(){
	var QNS = {};

	//常量
	QNS.CONSTANTS = {
		REGEXP:{
			mobile: /^1\d{10}$/,
			zipcode: /^\d{6}$/,
			lang_zh: /abc/,
			lang_latin: /^[A-Za-z]+$/,
			id_card: /^[1-9]\d{14}(\d{3})?$/
		}
	};

    //空函数
    QNS.noop = function(){};

	//工具箱
	QNS.util = {};

    /**
     * 生成唯一操作码
     * @returns {number}
     */
	QNS.util.genOpId = function(){
        var r;
        do {
            r = parseInt(Math.random().toString().substr(2, 6),10);
        } while ((r+'').length!=6);
        return r;
	};

    /**
     * 随机数
     * @param a
     * @param b
     * @param isInt
     * @returns {number}
     */
    QNS.util.random = function (a, b, isInt) {
        if (typeof a != 'number') {
            throw new TypeError('expected parameter "b" to be number,' + typeof a + ' provided');
        } else if (typeof b != 'number') {
            throw new TypeError('expected parameter "b" to be number,' + typeof b + ' provided');
        } else if (b <= a) {
            throw new RangeError('expected b to be greater than a');
        }
        var res = Math.random() * (Math.abs(b - a)) + a;
        return isInt ? parseInt(res) : res;
    };

    //EXPORT
    window.QNS = QNS;
})();