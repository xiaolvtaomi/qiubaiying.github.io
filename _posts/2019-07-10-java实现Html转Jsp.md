---
layout:     post
title:      java实现Html转Jsp
subtitle:   
date:       2019-07-10
author:     xiaolvtaomi
header-img: img/post-bg-desk.jpg
catalog: true
tags:
    - Android java html jsp applovin
---

交代下背景：公司自己做的广告素材导出后是html，分析了Applovin的调用方式，需要将html转成jsp交给applovin的模板显示广告。所以需要把Html转Jsp。对比了在线转jsp的结果发现其实做了以下几个动作（因为不懂html和jsp所以只能对比看转前后的差别）
* 写入前缀 al_renderHtml({"html":"
* 转义 /
* 转义 "
* 转义 \
* 写入后缀 "})

代码实现

```java

    public static void convertHtmlToJsp(Context context, String FOLER){
        new Thread(){
            public void run(){

                String JS_FILE_NAME = "index.js";
                String HTML_FINE_NAME = "index.html";
                String JS_PREFIX = "al_renderHtml({\"html\":\"";
                String JS_SUFIX = "\"})";

                BufferedReader br = null;
                FileWriter writer = null;
                try {
                    br = new BufferedReader(new FileReader(FOLDER + File.separator + HTML_FINE_NAME));
                    writer = new FileWriter(new File(FOLDER + File.separator + JS_FILE_NAME));

                    writer.write(JS_PREFIX);
                    String line;
                    char[] chars = new char[1024];
                    int len;
                    while ((len = br.read(chars)) > 0) {
                        line = String.copyValueOf(chars, 0, len);
                        writer.write(line.replace("\\", "\\\\").replace("\"", "\\\"").replace("/", "\\/"));
                    }
                    writer.write(JS_SUFIX);
                    writer.flush();
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    try {
                        if(br != null) {
                            br.close();
                        }
                        if(writer != null) {
                            writer.close();
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }.start();

    }

```