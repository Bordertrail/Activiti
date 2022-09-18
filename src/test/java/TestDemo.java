import org.activiti.engine.*;
import org.activiti.engine.repository.Deployment;
import org.activiti.engine.repository.ProcessDefinition;
import org.activiti.engine.repository.ProcessDefinitionQuery;
import org.activiti.engine.runtime.ProcessInstance;
import org.activiti.engine.task.Task;
import org.apache.commons.io.IOUtils;
import org.junit.Test;


import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

public class TestDemo {
    /**
     * 生成 activiti的数据库表
     */
    @Test
    public void testCreateDbTable() {
        //使用classpath下的activiti.cfg.xml中的配置创建processEngine
        ProcessEngine processEngine = ProcessEngines.getDefaultProcessEngine();
        System.out.println(processEngine);
    }

    @Test
    public void testDeployment() {
        //创建ProcessEngine
        ProcessEngine processEngine = ProcessEngines.getDefaultProcessEngine();
        //得到RepositoryService实例
        RepositoryService repositoryService = processEngine.getRepositoryService();
        //用RepositoryService来部署
        Deployment deployment = repositoryService.createDeployment()
                //添加bpmn资源
                .addClasspathResource("bpmn/evection.bpmn20.xml")
                //添加png资源
                .addClasspathResource("bpmn/diagram.png")
                .name("出差申请流程")
                .deploy();
        //输出部署信息
        System.out.println("流程部署id："+deployment.getId());
        System.out.println("流程部署名称："+deployment.getName());
    }

    @Test
    public void testStartProcess() {
        //获取ProcessEngine
        ProcessEngine processEngine = ProcessEngines.getDefaultProcessEngine();
        //获取运行时Service
        RuntimeService runtimeService = processEngine.getRuntimeService();
        //通过在xml定义的流程ID来启动流程
        ProcessInstance myEvection = runtimeService.startProcessInstanceByKey("evection");

        //输出信息
        System.out.println("流程ID:"+myEvection.getProcessDefinitionId());
        System.out.println("流程实例ID:"+myEvection.getId());
        System.out.println("当前活动ID:"+myEvection.getActivityId());
    }

    @Test
    public void testFindPersonalTask() {
        //声明负责人
        String assignee = "";
        ProcessEngine processEngine = ProcessEngines.getDefaultProcessEngine();
        //创建TaskService
        TaskService taskService = processEngine.getTaskService();
        //根据流程Key和任务负责人来查询任务
        List<Task> taskList = taskService.createTaskQuery().processDefinitionKey("evection").taskAssignee(assignee).list();

        for (Task task : taskList) {
            System.out.println("流程实例ID:"+task.getProcessInstanceId());
            System.out.println("任务ID:"+task.getId());
            System.out.println("任务负责人:"+task.getAssignee());
            System.out.println("任务名称："+task.getName());
            System.out.println("流程部署ID:"+task.getExecutionId());
            System.out.println("流程部署2ID:"+task.getParentTaskId());
            System.out.println("流程部署3ID:"+task.getTenantId());
        }
    }

    @Test
    public void completTask() {
        //获取引擎
        ProcessEngine processEngine = ProcessEngines.getDefaultProcessEngine();
        //获取taskService
        TaskService taskService = processEngine.getTaskService();
        Task task = taskService.createTaskQuery().processDefinitionKey("evection")
                .taskAssignee("")
                .singleResult();

        //输出任务ID,完成任务
        taskService.complete(task.getId());
    }

    @Test
    public void queryProcessDefinition() {

        ProcessEngine processEngine = ProcessEngines.getDefaultProcessEngine();

        RepositoryService repositoryService = processEngine.getRepositoryService();

        ProcessDefinitionQuery processDefinitionQuery = repositoryService.createProcessDefinitionQuery();

        List<ProcessDefinition> list = processDefinitionQuery.processDefinitionKey("evection").orderByProcessDefinitionVersion().desc().list();

        for (ProcessDefinition processDefinition : list) {
            System.out.println("流程定义 id="+processDefinition.getId());
            System.out.println("流程定义 name="+processDefinition.getName());
            System.out.println("流程定义 key="+processDefinition.getKey());
            System.out.println("流程定义 Version="+processDefinition.getVersion());
            System.out.println("流程部署ID ="+processDefinition.getDeploymentId());
            System.out.println("ResourceName ="+processDefinition.getResourceName());
            System.out.println("DiagramResourceName ="+processDefinition.getDiagramResourceName());
        }


    }



    @Test
    public void deleteDeployment() {
        ProcessEngine processEngine = ProcessEngines.getDefaultProcessEngine();
        //获取RepositoryService
        RepositoryService repositoryService = processEngine.getRepositoryService();
        repositoryService.deleteDeployment("7501",true);

    }


    @Test
    public void deleteTask() {
        ProcessEngine processEngine = ProcessEngines.getDefaultProcessEngine();

        TaskService taskService = processEngine.getTaskService();

        List<String> list = new ArrayList<String>();

        list.add("20005");
//        list.add("17505");
//        list.add("15005");
//        list.add("12505");

        for (String s : list) {
//            taskService.resolveTask(s);
            taskService.deleteTask(s);
        }

    }

    @Test
    public void  queryBpmnFile() throws IOException {
//        1、得到引擎
        ProcessEngine processEngine = ProcessEngines.getDefaultProcessEngine();
//        2、获取repositoryService
        RepositoryService repositoryService = processEngine.getRepositoryService();
//        3、得到查询器：ProcessDefinitionQuery，设置查询条件,得到想要的流程定义
        ProcessDefinition processDefinition = repositoryService.createProcessDefinitionQuery()
                .processDefinitionKey("evection")
                .singleResult();
//        4、通过流程定义信息，得到部署ID
        String deploymentId = processDefinition.getDeploymentId();
//        5、通过repositoryService的方法，实现读取图片信息和bpmn信息

        String x = processDefinition.getResourceName();
        String y = processDefinition.getDiagramResourceName();
//        png图片的流
        InputStream pngInput = repositoryService.getResourceAsStream(deploymentId,"bpmn/diagram.png");
//        bpmn文件的流
        InputStream bpmnInput = repositoryService.getResourceAsStream(deploymentId, processDefinition.getResourceName());
//        6、构造OutputStream流
        File file_png = new File("x:/diagram.png");
        File file_bpmn = new File("x:/evection.bpmn20.xml");
        FileOutputStream bpmnOut = new FileOutputStream(file_bpmn);
        FileOutputStream pngOut = new FileOutputStream(file_png);
//        7、输入流，输出流的转换
        IOUtils.copy(pngInput,pngOut);
        IOUtils.copy(bpmnInput,bpmnOut);
//        8、关闭流
        pngOut.close();
        bpmnOut.close();
        pngInput.close();
        bpmnInput.close();
    }


    @Test
    public void addBusinessKey(){
//        1、得到ProcessEngine
        ProcessEngine processEngine = ProcessEngines.getDefaultProcessEngine();
//        2、得到RunTimeService
        RuntimeService runtimeService = processEngine.getRuntimeService();
//        3、启动流程实例，同时还要指定业务标识businessKey，也就是出差申请单id，这里是1001
        ProcessInstance processInstance = runtimeService.
                startProcessInstanceByKey("evection","1001");
//        4、输出processInstance相关属性
        System.out.println("业务id=="+processInstance.getBusinessKey());

    }


    @Test
    public void queryProcessInstance() {
        // 流程定义key
        String processDefinitionKey = "evection";
        ProcessEngine processEngine = ProcessEngines.getDefaultProcessEngine();
        // 获取RunTimeService
        RuntimeService runtimeService = processEngine.getRuntimeService();
        List<ProcessInstance> list = runtimeService
                .createProcessInstanceQuery()
                .processDefinitionKey(processDefinitionKey)//
                .list();

        for (ProcessInstance processInstance : list) {
            System.out.println("----------------------------");
            System.out.println("流程实例id："
                    + processInstance.getProcessInstanceId());
            System.out.println("所属流程定义id："
                    + processInstance.getProcessDefinitionId());
            System.out.println("是否执行完成：" + processInstance.isEnded());
            System.out.println("是否暂停：" + processInstance.isSuspended());
            System.out.println("当前活动标识：" + processInstance.getActivityId());
        }
    }


}
