/**
 * SQLite
 * @constructor
 */
function ClsDb(dbName) {
    this._init();

    this.dbName = dbName;
    this._opId = ClsDb.genOpId();
}

/**
 * 生成操作码
 * @returns {number}
 */
ClsDb.genOpId = function () {
    var r;
    do {
        r = parseInt(Math.random().toString().substr(2, 6), 10);
    } while (r < 100000);
    return r;
};

/**
 * noop
 */
ClsDb.noop = function () {
};

/**
 * callback sequence
 * @type {{}}
 */
ClsDb.cbSeqOpen = {};
ClsDb.cbSeqClose = {};
ClsDb.cbSeqExecute = {};
ClsDb.cbSeqSelect = {};
ClsDb.cbSeqTransaction = {};

/**
 * 打开数据库
 * @param success
 * @param error
 */
ClsDb.prototype.open = function (success, error) {
    ClsDb.cbSeqOpen[this._opId] = {
        context: this,
        success: success || ClsDb.noop,
        error: error || ClsDb.noop
    };
    uexDataBaseMgr.openDataBase(this.dbName, this._opId);
};

/**
 * 关闭数据库
 * @param success
 * @param error
 */
ClsDb.prototype.close = function (success, error) {
    ClsDb.cbSeqClose[this._opId] = {
        context: this,
        success: success || ClsDb.noop,
        error: error || ClsDb.noop
    };
    uexDataBaseMgr.closeDataBase(this.dbName, this._opId);
};

/**
 * 查询
 * @param then
 * @param sql
 */
ClsDb.prototype.select = function (then, sql) {
    ClsDb.cbSeqSelect[this._opId] = {
        context: this,
        then: then || ClsDb.noop
    };
    uexDataBaseMgr.selectSql(this.dbName, this._opId, sql);
};

/**
 * 执行
 * @param success
 * @param error
 * @param sql
 */
ClsDb.prototype.execute = function (success, error, sql) {
    ClsDb.cbSeqExecute[this._opId] = {
        context: this,
        success: success || ClsDb.noop,
        error: error || ClsDb.noop
    };
    uexDataBaseMgr.executeSql(this.dbName, this._opId, sql);
};

/**
 * 事务
 * @param success
 * @param error
 * @param func
 */
ClsDb.prototype.transaction = function (success, error, func) {
    ClsDb.cbSeqTransaction[this._opId] = {
        context: this,
        success: success || ClsDb.noop,
        error: error || ClsDb.noop
    };
    uexDataBaseMgr.transaction(this.dbName, this._opId, func);
};

//初始化
ClsDb.prototype._init = function () {
    if (!ClsDb.hasInit) {
        /**
         * 打开数据库回调
         * @param opId
         * @param dataType
         * @param data
         */
        uexDataBaseMgr.cbOpenDataBase = function (opId, dataType, data) {
            var handlerObject = ClsDb.cbSeqOpen[opId];
            if(typeof handlerObject == 'object'){
                handlerObject[['success', 'error'][data]].call(handlerObject['context'], data);
            }
        };

        /**
         * 关闭数据库回调
         * @param opId
         * @param dataType
         * @param data
         */
        uexDataBaseMgr.cbCloseDataBase = function (opId, dataType, data) {
            var handlerObject = ClsDb.cbSeqClose[opId];
            if(typeof handlerObject == 'object'){
                handlerObject[['success', 'error'][data]].call(handlerObject['context'], data);
            }
        };

        /**
         * 查询回调
         * @param opId
         * @param dataType
         * @param data
         */
        uexDataBaseMgr.cbSelectSql = function (opId, dataType, data) {
            var handlerObject = ClsDb.cbSeqSelect[opId];
            if(typeof handlerObject == 'object'){
                handlerObject['then'].call(handlerObject['context'], data);
            }
        };

        /**
         * 执行回调
         * @param opId
         * @param dataType
         * @param data
         */
        uexDataBaseMgr.cbExecuteSql = function (opId, dataType, data) {
            var handlerObject = ClsDb.cbSeqExecute[opId];
            if(typeof handlerObject == 'object'){
                handlerObject[['success', 'error'][data]].call(handlerObject['context'], data);
            }
        };

        /**
         * 事务回调
         * @param opId
         * @param dataType
         * @param data
         */
        uexDataBaseMgr.cbTransaction = function (opId, dataType, data) {
            var handlerObject = ClsDb.cbSeqTransaction[opId];
            if(typeof handlerObject == 'object'){
                handlerObject[['success', 'error'][dataType]].call(handlerObject['context'], data);
            }
        };

        //初始化flag
        ClsDb.hasInit = true;
    }
};