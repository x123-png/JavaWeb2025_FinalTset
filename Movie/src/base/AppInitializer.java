package base;

import jakarta.servlet.ServletContext;
import jakarta.servlet.ServletContextEvent;
import jakarta.servlet.ServletContextListener;
import jakarta.servlet.annotation.WebListener;
import repo.DatabaseService;

@WebListener
public class AppInitializer implements ServletContextListener {

    @Override
    public void contextInitialized(ServletContextEvent sce) {
        System.out.println("........... Movie app context start");
        DatabaseService service = DatabaseService.getInstance();
        service.init();
        ServletContext context = sce.getServletContext();
        context.setAttribute(DatabaseService.CONTEXT_KEY, service);
    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        ServletContext context = sce.getServletContext();
        DatabaseService service = (DatabaseService)context.getAttribute(DatabaseService.CONTEXT_KEY);
        service.closeDataSource();
        System.out.println("........... close DatabaseService");
        System.out.println("........... Movie app context destroy");
    }
}