$(function () {
    $.ajax({
        type: "GET",
        url: "sys/generator/databases",
        contentType: "application/json",
        success: function(r){
            if(r.code === 0){
                vm.databases = r.data;
            }else{
                alert(r.msg);
            }
        }
    });
    $("#jqGrid").jqGrid({
        url: 'sys/generator/list',
        datatype: "json",
        colModel: [			
			{ label: '表名', name: 'tableName', width: 100, key: true },
			{ label: 'Engine', name: 'engine', width: 70},
            { label: '是否为空', name: 'nullable', width: 100 },
			{ label: '表备注', name: 'tableComment', width: 100 },
			{ label: '创建时间', name: 'createTime', width: 100 }
        ],
		viewrecords: true,
        height: 520,
        rowNum: 30,
		rowList : [10,30,50,100,200],
        rownumbers: true, 
        rownumWidth: 25, 
        autowidth:true,
        multiselect: true,
        pager: "#jqGridPager",
        jsonReader : {
            root: "page.list",
            page: "page.currPage",
            total: "page.totalPage",
            records: "page.totalCount"
        },
        prmNames : {
            page:"page", 
            rows:"limit", 
            order: "order"
        },
        postData:{"schemaName":vm.q.schemaName},
        gridComplete:function(){
        	//隐藏grid底部滚动条
        	$("#jqGrid").closest(".ui-jqgrid-bdiv").css({ "overflow-x" : "hidden" }); 
        }
    });
});

var vm = new Vue({
	el:'#hqapp',
	data:{
	    databases: [],
        showList: true,
        modelNames: [
            {name:"请自己填写模块名称]",value:""}
            ],
        tables: [],
		q:{
			tableName: null,
            schemaName: ""
		},
        generateModel: {
            basePackage: '',
            moduleName: '',
            tbPrefix: null
        },
        fieldRules: {
            basePackage: [
                {required: true, message: '请输入基础包名', trigger: 'blur'}
            ],
            moduleName: [
                {required: true, message: '请输入模块名称', trigger: 'blur'}
            ]
        },
        activeNames: ['1'],
	},
	methods: {
		query: function () {
			$("#jqGrid").jqGrid('setGridParam',{ 
                postData:{'tableName': vm.q.tableName,"schemaName":vm.q.schemaName,},
                page:1 
            }).trigger("reloadGrid");
		},
        handleChange:function(val) {
            console.log(val);
        },
        pannelChange: function(val){
		    vm.activeNames = [val];
        },
        chooseModule: function(){
		    vm.moduleName = vm.modelName;
        },
        configGen: function(){
            vm.tables = getSelectedRows();
            if( vm.tables == null){
                return ;
            }
            vm.showList =false;
            vm.activeNames = ['2'];
        },
		generator: function() {
            if(vm.validator()){
                return ;
            }
            var tableNames = getSelectedRows();
            if(tableNames == null){
                return ;
            }
            location.href = "sys/generator/code?tables=" + tableNames.join()
                +"&schemaName="+vm.q.schemaName +"&moduleName="+vm.generateModel.moduleName+"&basePackage="+vm.generateModel.basePackage;
		},
        validator: function () {
            if(isBlank(vm.generateModel.basePackage)){
                alert("基础包路径不能为空");
                return true;
            }
		    if(isBlank(vm.generateModel.moduleName)){
                alert("模块名称不能为空");
                return true;
            }
        },
        reload: function () {
            vm.showList = true;
            var page = $("#jqGrid").jqGrid('getGridParam','page');
            $("#jqGrid").jqGrid('setGridParam',{
                postData:{'tableName': vm.q.tableName,"schemaName":vm.q.schemaName},
                page:page
            }).trigger("reloadGrid");
        }
	}
});

