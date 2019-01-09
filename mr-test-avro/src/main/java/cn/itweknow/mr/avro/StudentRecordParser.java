package cn.itweknow.mr.avro;

import org.apache.hadoop.io.Text;

/**
 * @author ganchaoyang
 * @date 2019/1/7 16:57
 * @description
 */
public class StudentRecordParser {

    // 姓名
    private String name;

    // 年龄
    private int age;

    // 爱好
    private String hobby;

    // 班级
    private String clazz;

    /**
     * 解析数据，将字符串转为一个对象。
     * @param recordStr
     */
    public void parse(String recordStr) {
        String[] strings = recordStr.split("\t");
        if (strings.length < 4) {
            return;
        }
        this.name = strings[0];
        try {
            this.age = Integer.parseInt(strings[1]);
        } catch (Exception e) {
            this.age = 0;
        }
        this.hobby = strings[2];
        this.clazz = strings[3];
    }

    public void parse(Text text) {
        parse(text.toString());
    }

    /**
     * 判断数据是否合法。
     * @return
     */
    public boolean isValid() {
        return age>0 && name != null;
    }


    public String getName() {
        return name;
    }

    public StudentRecordParser setName(String name) {
        this.name = name;
        return this;
    }

    public int getAge() {
        return age;
    }

    public StudentRecordParser setAge(int age) {
        this.age = age;
        return this;
    }

    public String getHobby() {
        return hobby;
    }

    public StudentRecordParser setHobby(String hobby) {
        this.hobby = hobby;
        return this;
    }

    public String getClazz() {
        return clazz;
    }

    public StudentRecordParser setClazz(String clazz) {
        this.clazz = clazz;
        return this;
    }
}
