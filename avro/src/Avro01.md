### 什么是Avro
Avro是一个独立于编程语言的数据序列化系统。这个项目由Ddoug Cutting（Hadoop之父）创建，目标是解决Hadoop中Writable类型缺乏语言的可移植性的不足。Avro模式通常采用JSON来写，数据则采用二进制格式编码，也可采用基于JSON的数据编码方式。
### Avro的数据类型和模式
Avro定义了一些基本的数据类型，我们可以用他们来构建应用特定的数据结构。下面的表格我们列举了Avro的基本类型。  

| 类型 | 描述 | 模式示例 |  
| ------ | ------ | ------ |  
| null | 空值 | "null"  |   
|  boolean | 二进制值 | "boolean" |   
| int | 32位带符号整数 | "int" |   
|  long  | 64位带符号整数 | "long"  |   
| float | 单精度（32位）浮点数 | "float" |  
| double | 双精度（64位）浮点数 | "double" |  
| bytes | 8位无符号字节序列 | "bytes" |  
| string | Unicode字符序列 | "string" |  

还有一些复杂的类型如下表所示：

| 类型 | 描述 | 模式示例 |  
| ------ | ------ | ------ |  
| array | 一个排过序的对象集合。特定数组中的所有对象必须模式相同。 | {"type": "array","items": "long"}  |   
|  map | 未排过序的键-值对。键必须是字符串，值可以是任何一种类型，但是某一个map内的所有值必须模式相同。 | {"type": "map", "values": "string"} |   
| record | 一个任意类型的命名字段集合。（相当于java中的自定义对象） | {"type": "record", "name": "User", "doc":"A User Desc","fileds":[{"name":"nickname","type": "string"},{"name":"age","type":"int"}]} |   
|  enum  | 一个命名的值集合（枚举） | {"type":"enum","name":"ActionStatus","doc":"操作状态","symbols":["SUCCESS","FAILED","ACTING"]}  |   
| fixed | 一组固定数量的8位无符号字节 | {"type":"fixed","name":"Md5Hash","size":16} |  
| union | 模式的并集。并集可用JSON数组表示，其中每个元素为一个模式。并集表示的数据必须与其内的某个模式相匹配 | ["null","string",{"type":"map","values":"string"}] |  


### Avro数据文件
前面也提到过设计Avro的目的就是解决Hadoop中Writable类型缺乏语言的可移植性的不足。Avro数据文件主要是面向跨语言使用而设计的，我们可以通过Java写入文件，然后通过Python来读取文件，这都是没有问题的。数据文件的头部包含一个Avro模式和一个同步标识（sync marker）,然后紧接着是一系列包含序列化Avro对象的数据块。数据块通过sync marker分隔。
这里有两个概念解释一下：
* Avro模式 - 其实就相当于对象的定义，在这里我们规定字段的类型以及描述等信息。
* sync marker - 对与该文件来讲是唯一的，存储在文件头部。

### 序列化和反序列化
上面也简单的了解了一下Avro，下面我们通过两段代码来尝试一下Avro的序列化和反序列化。

* Avro模式定义，User.avsc
```json
{
    "type": "record",
    "name": "User",
    "doc": "一个用户",
    "fields": [
        {"name": "name", "type": "string"},
        {"name": "age", "type": "int"}
    ]
}
```

* 序列化

```java
@Test
public void write() throws IOException {
    Schema.Parser parser = new Schema.Parser();
    InputStream in = this.getClass().getResourceAsStream("User.avsc");
    Schema schema = parser.parse(in);
    GenericRecord record = new GenericData.Record(schema);
    record.put("name", "ganchaoyang");
    record.put("age", 23);

    File file = new File("result.avro");
    DatumWriter<GenericRecord> writer = new GenericDatumWriter<GenericRecord>(schema);
    try(DataFileWriter<GenericRecord> dataFileWriter = new DataFileWriter<GenericRecord>(writer)) {
        dataFileWriter.create(schema, file);
        dataFileWriter.append(record);
    }
}
```

* 反序列化

```java
@Test
public void read() throws IOException {
    File file = new File("result.avro");
    DatumReader<GenericRecord> reader = new GenericDatumReader<>();
    try (DataFileReader<GenericRecord> dataFileReader = new DataFileReader<GenericRecord>(file, reader)) {
        GenericRecord record;
        while (dataFileReader.hasNext()) {
            record = dataFileReader.next();
            Assert.assertEquals("ganchaoyang", record.get("name").toString());
            Assert.assertEquals(23, record.get("age"));
        }
    }
}
```

想要源码？[戳这里。]()