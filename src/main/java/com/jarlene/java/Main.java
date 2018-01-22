package com.jarlene.java;

import static com.jarlene.java.utils.ReflectUtil.*;

public class Main {


    public static void main(String[] args) {

        // ReflectUtil demo
        String sub = on("java.lang.String")
                .create("hahah")
                .call("substring", 2)
                .get();
        System.out.println(sub);


        // ContainerUtil demo

    }
}
