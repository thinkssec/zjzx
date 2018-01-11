package com.boot;

/**
 * 
 * web应用启动器
 * 
 * @author CrazyPig
 * @since 2016-08-03
 *
 */
public class Launcher {

    public static final int DEFAULT_PORT = 8090;
    public static final String DEFAULT_APP_NAME="/zjzx";
    public static final String DEFAULT_CONTEXT_PATH = "/jetty-embeded-webapp";
    private static final String DEFAULT_APP_CONTEXT_PATH = "web";


    public static void main(String[] args) {

        //runJettyServer(DEFAULT_PORT, DEFAULT_CONTEXT_PATH);
        //runJettyServer(DEFAULT_PORT, DEFAULT_APP_NAME);

    }
/*
    public static void runJettyServer(int port, String contextPath) {

        //Server server = createJettyServer(port, contextPath);
        Server server = createDevServer(port, contextPath);
        try {
            server.start();
            server.join();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                server.stop();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

    }

    public static Server createJettyServer(int port, String contextPath) {

        Server server = new Server(port);
        server.setStopAtShutdown(true);

        ProtectionDomain protectionDomain = Launcher.class.getProtectionDomain();
        URL location = protectionDomain.getCodeSource().getLocation();
        String warFile = location.toExternalForm();

        WebAppContext context = new WebAppContext(warFile, contextPath);
        context.setServer(server);

        // 设置work dir,war包将解压到该目录，jsp编译后的文件也将放入其中。
        String currentDir = new File(location.getPath()).getParent();
        File workDir = new File(currentDir, "work");
        context.setTempDirectory(workDir);

        server.setHandler(context);
        return server;

    }

    public static Server createDevServer(int port, String contextPath) {

        Server server = new Server();
        server.setStopAtShutdown(true);
        *//*Field f = null;
        try {
            f =TldScanner.class.getDeclaredField("systemUris");
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        }

        f.setAccessible(true);

        try {
            ((Set<?>) f.get(null)).clear();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }

        f.setAccessible(false);*//*
        ServerConnector connector = new ServerConnector(server);
        // 设置服务端口
        connector.setPort(port);
        connector.setReuseAddress(false);
        server.setConnectors(new Connector[] {connector});

        // 设置web资源根路径以及访问web的根路径
        WebAppContext webAppCtx = new WebAppContext(DEFAULT_APP_CONTEXT_PATH, contextPath);
        webAppCtx.setDescriptor(DEFAULT_APP_CONTEXT_PATH + "/WEB-INF/web.xml");
        webAppCtx.setResourceBase(DEFAULT_APP_CONTEXT_PATH);

        webAppCtx.setClassLoader(Thread.currentThread().getContextClassLoader());
        try {
            //webAppCtx.setClassLoader(new WebAppClassLoader(DEFAULT_APP_CONTEXT_PATH.getClass().getClassLoader(), webAppCtx));
        } catch (Exception e) {
            e.printStackTrace();
        }
        server.setHandler(webAppCtx);

        return server;
    }*/

}
