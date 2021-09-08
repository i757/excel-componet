# excel-component

## 导入

### 使用

1. 后端

   - 继承 `AbstractExcelHandler.class` 类，实现 `importExcel(Row row,Object... params)` 方法。该方法用于实现业务逻辑，其中参数 `row`表示excel的行数据，`params`表示处理业务逻辑时所需的参数

   - 调用 `ImportComponent`中的 `importExcel`方法，进行excel导入。

     ```java
     void importExcel(InputStream inputStream, String sessionId, Class<? extends AbstractExcelHandler> clazz, boolean hasTitle, Object... params);
     void importExcel(File file, String sessionId, Class<? extends AbstractExcelHandler> clazz,boolean hasTitle,Object... params);
     void importExcel(MultipartFile multipartFile, String sessionId, Class<? extends AbstractExcelHandler> clazz,boolean hasTitle,Object... params);
     ```

     参数说明：

     |            **属性**            | **说明**                               |
     | :----------------------------: | -------------------------------------- |
     | inputStream/file/multipartFile | excel文件/输入流                       |
     |           sessionId            | 当前会话id                             |
     |             clazz              | 继承AbstractExcelHandler.class的类对象 |
     |            hasTitle            | excel是否包含标题                      |
     |             params             | 处理业务逻辑时所需的参数               |

     

2. 前端
   
   调用 `ws://ip:port/excel/websocket`进行session注册，进行导入时，后台每隔一秒会发送socket消息。例如：
   
   ```json
   {
       "sheetProgressMap":{
           "1":{
               "dealCount":7782,
               "msg":"当前正在处理第 1 个sheet页，进度：0.77",
               "progress":0.77,
               "sheet":1,
               "totalCount":10000
           },
           "2":{
               "dealCount":3884,
               "msg":"当前正在处理第 2 个sheet页，进度：0.77",
               "progress":0.77,
               "sheet":2,
               "totalCount":5000
           }
       },
       "totalProgress":0.77
   }
   ```
   
   参数说明：
   
   |     **属性**     | **说明**                    |
   | :--------------: | --------------------------- |
   |  totalProgress   | excel导入进度，范围 0~1之间 |
   | sheetProgressMap | 每个sheet的导入进度         |
   |    totalCount    | sheet的总行数，不包括title  |
   |    dealCount     | sheet已经导入的条数         |
   |      sheet       | sheet编号                   |
   |     progress     | sheet导入进度               |
   |       msg        | 消息                        |
   
   

### demo

http://10.10.16.8/luogy/demo.git
