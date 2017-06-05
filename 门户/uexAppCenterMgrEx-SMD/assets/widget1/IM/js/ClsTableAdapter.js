/**
 * 表适配器
 * @constructor
 */
function ClsTableAdapter(tblName) {
    this.tblName = tblName;
    this.table = ClsTableAdapter.tables[this.tblName];
    this.db = new ClsDb(this.table.db);
}

//noop
ClsTableAdapter.noop = function () {
};

ClsTableAdapter.getTable = function (tblName) {
    return new ClsTableAdapter(tblName);
};

/**
 * 执行代理
 * @param cbInitTbl
 */
ClsTableAdapter.prototype.tblAgent = function (cbInitTbl) {
    var self = this, tbDef = self.table.def;
    this.db.open(function (data) {
        console.log('Database connect ok');
        self.db.execute(function () {
            console.log('Table initialize ok');
            if (cbInitTbl) {
                cbInitTbl.call(self);
            }
        }, function () {
            throw new Error('Fail to initialize table: ' + self.tblName);
        }, tbDef);
    }, function (data) {
        throw new Error('Fail to initialize database');
    });
};

/**
 * 转换数组格式为可查询的sql column格式
 * @param Column
 * @returns {string}
 * @private
 */
ClsTableAdapter.prototype._array2cols = function (Column) {
    if (!Column.length) {
        throw new Error('Column can not be empty!');
    } else {
        var s = JSON.stringify(Column);
        return s.substr(1, s.length - 2);
    }
};

/**
 * 转换字段数组为SQL语句cols部件的形式
 * @param cols
 * @returns {string}
 * @private
 */
ClsTableAdapter.prototype._getQueryCols = function (cols) {
    var f = [];
    if (cols instanceof Array) {
        cols.forEach(function (o) {
            f.push('`' + o + '`');
        });
    }
    return f.length ? f.toString() : '*';
};

/**
 * curd: 增
 * @param success
 * @param error
 * @param cols {object}
 */
ClsTableAdapter.prototype.c = function (success, error, cols) {
    var self = this;
    var keys = [], vals = [], fields = this.table.fields;
    for (var key in cols) {
        if (cols.hasOwnProperty(key) && fields.indexOf(key) != -1) {
            keys.push(key);
            vals.push(cols[key]);
        }
    }
    var sql = 'INSERT OR REPLACE INTO ' + this.tblName + '(' + this._array2cols(keys) + ')  values(' + this._array2cols(vals) + ')';
    console.log('INSERT: %s', sql);

    this.tblAgent(function(){
        self.db.execute(function (data) {
            (success || ClsTableAdapter.noop).call(self, data);
        }, function (data) {
            (error || ClsTableAdapter.noop).call(self, data);
        }, sql);
    });
};

/**
 * curd: 删
 * eg.
 * func(cb1, cb2, [1,2,3], 'in', 'id');
 * func(cb1, cb2, {col1: val1, col2: val2});
 * func(cb1, cb2, 'age<20 and gender=male');
 *
 * @param success
 * @param error
 * @param where {array|object|string}
 * @param operator {string}
 * @param column {string}
 */
ClsTableAdapter.prototype.d = function (success, error, where, operator, column) {
    var self = this, cods = [], fields = this.table.fields;
    var _where = ' where ';

    if (operator == 'in' && where.length) {
        _where += column + ' in (' + this._array2cols(where) + ')';
    } else if (where instanceof Object) {
        for (var k in where) {
            if (where.hasOwnProperty(k) && fields.indexOf(k) != -1) {
                //todo: 需要更加健壮的方案，下同
                cods.push(k + '="' + where[k] + '"');
            }
        }
        _where += ' ' + cods.join(' and ') + ' ';
    } else if (typeof where == 'string' && where != '') {
        _where += where;
    } else {
        _where = '';
    }

    var sql = 'DELETE FROM ' + this.tblName + _where;
    console.log('DELETE: %s'+ sql);

    this.tblAgent(function(){
        self.db.execute(function (data) {
            (success || ClsTableAdapter.noop).call(self, data);
        }, function (data) {
            (error || ClsTableAdapter.noop).call(self, data);
        }, sql);
    });
};

/**
 * curd: 改
 * @param then
 * @param colName
 * @param colValue
 * @param where
 * @param operator
 * @param column
 */
ClsTableAdapter.prototype.u = function (then, colName, colValue, where, operator, column) {
    var self = this, cods = [], fields = this.table.fields;
    var _where = ' where ';

    if (operator == 'in' && where.length) {
        _where += column + ' in (' + this._array2cols(where) + ')';
    } else if (where instanceof Object) {
        for (var k in where) {
            if (where.hasOwnProperty(k) && fields.indexOf(k) != -1) {
                cods.push(k + '="' + where[k] + '"');
            }
        }
        _where += ' ' + cods.join(' and ') + ' ';
    } else if (typeof where == 'string' && where != '') {
        _where += where;
    } else {
        _where = '';
    }

    var sql = 'UPDATE ' + this.tblName + ' SET ' + colName + '="' + colValue + '" ' + _where;
    console.log('UPDATE: %s', sql);

    this.tblAgent(function(){
        self.db.select(function (data) {
            (then || ClsTableAdapter.noop).call(self, data);
        }, sql);
    });
};

/**
 * curd: 查
 * @param then
 * @param where
 * @param operator
 * @param column
 * @param cols
 */
ClsTableAdapter.prototype.r = function (then, where, operator, column, cols) {
    var self = this;
    var cods = [], fields = this.table.fields;
    var _where = ' where ';

    if (operator == 'in' && where.length) {
        _where += column + ' in (' + this._array2cols(where) + ')';
    } else if (where instanceof Object) {
        for (var k in where) {
            if (where.hasOwnProperty(k) && fields.indexOf(k) != -1) {
                cods.push(k + '="' + where[k] + '"');
            }
        }
        _where += ' ' + cods.join(' and ') + ' ';
    } else if (typeof where == 'string' && where != '') {
        _where += where;
    } else {
        _where = '';
    }

    var _cols = this._getQueryCols(cols);
    var sql = 'SELECT ' + _cols + ' FROM ' + this.tblName + _where;
    console.log('Select: %s', sql);

    this.tblAgent(function(){
        self.db.select(function (data) {
            (then || ClsTableAdapter.noop).call(self, data);
        }, sql);
    });
};

//tables
ClsTableAdapter.tables = {
    'cttq_cache_multi': {
        db: 'cttq',
        def: 'CREATE TABLE IF NOT EXISTS "cttq_cache_multi" ("id" INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT, "hecate" TEXT, "url_from" TEXT, "url_to" TEXT, "real_path" TEXT, "size" integer, "time" TEXT, CONSTRAINT "hecate" UNIQUE (hecate COLLATE NOCASE ASC))',
        fields: ['id', 'hecate', 'url_from', 'url_to', 'real_path', 'size', 'time']
    }
};
