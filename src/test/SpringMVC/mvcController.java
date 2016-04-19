package test.SpringMVC;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;
import test.SpringMVC.Model.Person;
import test.SpringMVC.util.JSON;
import test.SpringMVC.util.adb;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;

/**
 * Created by Himx on 30/3/2016.
 */

@Controller
public class mvcController {
    private static adb myadb = new adb();

    @RequestMapping("/")
    public ModelAndView index() {
        ModelAndView mav = new ModelAndView("index");
        mav.addObject("adb", myadb);
        return mav;
    }

    @RequestMapping("/hello")
    public String hello() {
        return "hello";
    }

    @RequestMapping("/person1/{id}")
    public ModelAndView toPerson(@PathVariable(value="id") Integer id) {
        Person pp = new Person();
        pp.setAge(id);
        pp.setName("helloworld");
        ModelAndView mav = new ModelAndView("hello");
        mav.addObject("message", pp);
        return mav;
    }

    @RequestMapping(value="/person1/{id}", params="example")
    public ModelAndView toPerson2(@PathVariable(value="id") Integer id) {
        Person pp = new Person();
        pp.setAge(id - 1);
        pp.setName("example");
        ModelAndView mav = new ModelAndView("hello");
        mav.addObject("message", pp);
        return mav;
    }

    @RequestMapping(value = "/getDevices", method = RequestMethod.GET)
    @ResponseBody
    public String getDevices(HttpServletRequest request,HttpServletResponse response) throws IOException {
        return myadb.getDeviceInfo();
    }

    @RequestMapping(value = "/getLogcat", method = RequestMethod.GET)
    @ResponseBody
    public String getLogcat() throws IOException {
        myadb.logcat();
        return myadb.logcatInfo;
    }

    @RequestMapping(value = "/getEnergyInfo/{pid}", method = RequestMethod.GET)
    @ResponseBody
    public String getEnergyInfo(@PathVariable(value="pid") Integer pid) {
        return myadb.getEnergyInfo(pid);
    }

}
