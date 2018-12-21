package cn.itweknow.bigdata.io;

import org.apache.hadoop.io.Writable;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

/**
 * @author ganchaoyang
 * @date 2018/12/21 15:37
 * @description
 */
public class UserWriteable implements Writable {

    private String name;

    private String address;

    public String getName() {
        return name;
    }

    public UserWriteable setName(String name) {
        this.name = name;
        return this;
    }

    public String getAddress() {
        return address;
    }

    public UserWriteable setAddress(String address) {
        this.address = address;
        return this;
    }

    public void write(DataOutput dataOutput) throws IOException {
        dataOutput.writeChars(name);
        dataOutput.writeChars(address);
    }

    public void readFields(DataInput dataInput) throws IOException {
        // 先进先出原则，与write方法对应
        name = dataInput.readLine();
        address = dataInput.readLine();
    }
}
