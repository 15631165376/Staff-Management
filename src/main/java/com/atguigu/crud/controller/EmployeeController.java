package com.atguigu.crud.controller;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.atguigu.crud.bean.Employee;
import com.atguigu.crud.bean.Msg;
import com.atguigu.crud.service.EmployeeService;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;

/**
 * ����Ա��CRUD����
 * 
 */
@Controller
public class EmployeeController {

	@Autowired
	EmployeeService employeeService;

	/**
	 * ������������һ
	 * ����ɾ����1-2-3
	 * ����ɾ����1
	 * @param id
	 * @return
	 */
	@ResponseBody
	@RequestMapping(value="/emp/{ids}",method=RequestMethod.DELETE)
	public Msg deleteEmp(@PathVariable("ids") String ids){
		//����ɾ��
		if(ids.contains("-"))
		{
			String[] str_ids = ids.split("-");
			//���id�ļ���
			List<Integer> del_ids = new ArrayList<Integer>();
			for (String string : str_ids) {
				del_ids.add(Integer.parseInt(string));
			}
			employeeService.deleteBatch(del_ids);
		}else{
			Integer id = Integer.parseInt(ids);
			employeeService.deleteEmp(id);
		}
		return Msg.success();		
	}
	
	
	/**
	 * ���ֱ�ӷ���ajax=PUT��ʽ������
	 * ���⣺�������������ݣ�����Employee�����װ����
	 * ԭ��
	 * Tomcat��
	 * 		���������е����ݣ���װ��һ��map
	 * 		request.getParameter("empName") �ͻ�����map��ȥ��
	 * 		SpringMVC ��װPOJO�����ʱ��
	 * 				���POJO�е�ÿ�����Ե�ֵ��request.getParameter()�õ�
	 * AJAX����PUT����������Ѫ����
	 * 		PUT�����������е����ݣ�request.getParemeter()�ò���
	 * 		Tomcatһ����PUT����Ͳ����װ�������е�����Ϊmap��ֻ��POST��ʽ������������е����ݲŻ��װ��map
	 * 
	 * ���������
	 * ����Ҫ��֧��ֱ�ӷ���PUT֮�������Ҫ��װ�������е�����
	 * ������HttpPutFormContentFilter
	 * ���ã����������е����ݽ�����װ��һ��map
	 * request�����°�װ��request.getParameter����д���ͻ���Լ���װ��map��ȡ����
	 * SpringMVC�ṩ�˽������
	 * 
	 * Ա�����·���
	 * @param employee
	 * @return
	 */
	@ResponseBody
	@RequestMapping(value="/emp/{empId}",method=RequestMethod.PUT)
	public Msg saveEmp(Employee employee){
		System.out.println("��Ҫ���¶�Ա�����ݣ�"+employee.toString());
		employeeService.updateEmp(employee);
		return Msg.success();
	}
	
	/**
	 * ����id��ѯ
	 * @param id
	 * @return
	 */
	@ResponseBody
	@RequestMapping(value="/emp/{id}",method=RequestMethod.GET)
	public Msg getEmp(@PathVariable("id") Integer id){
		Employee employee = employeeService.getEmp(id);
		return Msg.success().add("emp",employee);
	}
	
	/**
	 * ����û����Ƿ����
	 * 1��֧��JSR303У��
	 * 2������Hibernate-Validator��
	 * @param empName
	 * @return
	 */
	@ResponseBody
	@RequestMapping("/checkuser")
	public Msg checkUser(@RequestParam("empName") String empName){
		//���ж��û����Ƿ��ǺϷ��ı��ʽ
		String regex = "(^[a-zA-Z0-9_-]{6,16}$)|(^[\u2E80-\u9FFF]{2,5})";
		if(!empName.matches(regex)){
			return Msg.fail().add("va_msg","�û���������6-16λӢ�ĺ����ֵ���ϻ���2-5λ����");
		}
		boolean b = employeeService.checkUser(empName);
		if(b){
			return Msg.success();
		}else{
			return Msg.fail().add("va_msg","�û���������");
		}
	}
	
	@ResponseBody
	@RequestMapping(value="/emp",method=RequestMethod.POST)
	public Msg saveEmp(@Valid Employee employee,BindingResult result){
		if(result.hasErrors()){
			//У��ʧ�ܣ�����ʧ��,��ģ̬������ʾ�������Ϣ
			Map<String,Object> map = new HashMap<String,Object>();
			List<FieldError> errors = result.getFieldErrors();
			for (FieldError fieldError : errors) {
				System.out.println("������ֶ�"+fieldError.getField());
				System.out.println("������Ϣ��"+fieldError.getDefaultMessage());
				map.put(fieldError.getField(), fieldError.getDefaultMessage());
			}
			return Msg.fail().add("errorField", map);
		}else{
			employeeService.saveEmp(employee);
		}
		return Msg.success();
	}
	
	/**
	 * ResponseBody ����������Ҫ����Jackson�������𽫷��ص������Զ�ת��Ϊjson����
	 * @param pn
	 * @param model
	 * @return
	 */
	@ResponseBody
	@RequestMapping(value="/emps")
	public Msg getEmpsWithJson(
			@RequestParam(value = "pn", defaultValue = "1") Integer pn,
			Model model) {
		// �ⲻ��һ����ҳ��ѯ��
		// ����PageHelper��ҳ���
		// �ڲ�ѯ֮ǰֻ��Ҫ���ã�����ҳ�룬�Լ�ÿҳ�Ĵ�С
		PageHelper.startPage(pn, 5);
		// startPage��������������ѯ����һ����ҳ��ѯ
		List<Employee> emps = employeeService.getAll();
		// ʹ��pageInfo��װ��ѯ��Ľ����ֻ��Ҫ��pageInfo����ҳ������ˡ�
		// ��װ����ϸ�ķ�ҳ��Ϣ,���������ǲ�ѯ���������ݣ�����������ʾ��ҳ��
		PageInfo page = new PageInfo(emps, 5);
		return Msg.success().add("pageInfo",page);
	}

	//@RequestMapping("/emps")
	public String getEmps(
			@RequestParam(value = "pn", defaultValue = "1") Integer pn,
			Model model) {
		// �ⲻ��һ����ҳ��ѯ��
		// ����PageHelper��ҳ���
		// �ڲ�ѯ֮ǰֻ��Ҫ���ã�����ҳ�룬�Լ�ÿҳ�Ĵ�С
		PageHelper.startPage(pn, 5);
		// startPage��������������ѯ����һ����ҳ��ѯ
		List<Employee> emps = employeeService.getAll();
		// ʹ��pageInfo��װ��ѯ��Ľ����ֻ��Ҫ��pageInfo����ҳ������ˡ�
		// ��װ����ϸ�ķ�ҳ��Ϣ,���������ǲ�ѯ���������ݣ�����������ʾ��ҳ��
		PageInfo page = new PageInfo(emps, 5);
		model.addAttribute("pageInfo", page);

		return "list";
	}

}
