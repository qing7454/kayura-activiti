package org.kayura.activiti.editor.rest;

import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.activiti.engine.HistoryService;
import org.activiti.engine.RepositoryService;
import org.activiti.engine.RuntimeService;
import org.activiti.engine.TaskService;
import org.activiti.engine.task.Task;
import org.activiti.engine.task.TaskQuery;
import org.kayura.activiti.vo.TaskVo;
import org.kayura.type.PageList;
import org.kayura.type.PageParams;
import org.kayura.type.Paginator;
import org.kayura.utils.StringUtils;
import org.kayura.web.ui.UISupport;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class TaskRestResource {

	@Autowired
	protected TaskService taskService;

	@Autowired
	protected RepositoryService repositoryService;

	@Autowired
	protected HistoryService historyService;

	@Autowired
	protected RuntimeService runtimeService;

	@Autowired
	protected UISupport ui;

	@RequestMapping(value = "/task/{userId}/find", method = RequestMethod.GET, produces = "application/json")
	public Map<String, Object> findTaskList(HttpServletRequest req, @PathVariable String userId, String keyword) {

		PageParams pp = ui.getPageParams(req);
		TaskQuery taskQuery = taskService.createTaskQuery().taskCandidateOrAssigned(userId);

		if (StringUtils.isNotEmpty(keyword)) {
			taskQuery.taskNameLike("%" + keyword + "%");
		}

		long count = taskQuery.count();
		List<Task> list = taskQuery.listPage(pp.getOffset(), pp.getLimit());

		List<TaskVo> tasks = TaskVo.fromTasks(list);
		for (TaskVo task : tasks) {
			task.setVariables(runtimeService.getVariables(task.getExecutionId()));
		}

		PageList<TaskVo> pageList = new PageList<TaskVo>(tasks, new Paginator(count));
		return ui.putData(pageList);
	}

	@RequestMapping(value = "/task/{taskId}/{userId}/claim", method = RequestMethod.POST)
	@ResponseStatus(value = HttpStatus.OK)
	public void taskClaim(@PathVariable String taskId, @PathVariable String userId) {

		taskService.claim(taskId, userId);
	}
}
