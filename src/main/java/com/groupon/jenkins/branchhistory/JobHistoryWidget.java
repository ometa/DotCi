/*
The MIT License (MIT)

Copyright (c) 2014, Groupon, Inc.

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in
all copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
THE SOFTWARE.
 */
package com.groupon.jenkins.branchhistory;

import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import com.groupon.jenkins.dynamic.build.DynamicProject;
import com.groupon.jenkins.dynamic.build.DynamicProjectBranchTabsProperty;
import hudson.widgets.Widget;
import org.apache.commons.lang.StringUtils;
import org.kohsuke.stapler.Stapler;
import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.StaplerResponse;

import javax.servlet.ServletException;
import java.io.IOException;


public class JobHistoryWidget extends Widget{


    protected static final int BUILD_COUNT = 30;
    public static final String TAB_SELECTION = "tabSelection";
    private final DynamicProject project;
    private final Iterable<HistoryTab> tabs;
    private final HistoryTab currentTab;

    @Override
    public String getUrlName() {
        return "buildHistory";
    }


    public JobHistoryWidget(DynamicProject project) {
        this.project = project;
        this.tabs = intializeTabs();
        this.currentTab = getActiveTab(tabs);
        currentTab.setActive();
    }

    public Iterable<HistoryTab> getTabs(){
        return tabs;
    }
    public Iterable<HistoryTab> intializeTabs(){
        Iterable<HistoryTab> tabs = BuildHistoryTab.getTabs(project);
        return tabs;
    }

    public Object getDynamic(String token, StaplerRequest req, StaplerResponse rsp) {
        req.getSession().setAttribute(TAB_SELECTION + project.getName(), token);
        return findTab(token);
    }

    private HistoryTab findTab(final String tabUrl) {
       return Iterables.find(tabs, new Predicate<HistoryTab>() {
           @Override
           public boolean apply(HistoryTab historyTab) {
               return historyTab.getUrl().equals(tabUrl);
           }
       });
    }

    public void doAddTab(StaplerRequest req, StaplerResponse rsp) throws IOException, ServletException, InterruptedException {
        String tab  = req.getParameter("tab");
        if(StringUtils.isNotEmpty(tab)){
            DynamicProjectBranchTabsProperty branchTabsProperty = getTabsProperty();
            if(branchTabsProperty == null){
                branchTabsProperty =     new DynamicProjectBranchTabsProperty(tab);
                project.addProperty(branchTabsProperty);
            }else{
                branchTabsProperty.addBranch(tab);
            }
            project.save();
        }
        rsp.forwardToPreviousPage(req);
    }

    public void doRemoveTab(StaplerRequest req, StaplerResponse rsp) throws IOException, ServletException, InterruptedException {
        String tab  = req.getParameter("tab");
        if(StringUtils.isNotEmpty(tab)){
            DynamicProjectBranchTabsProperty branchTabsProperty = getTabsProperty();
            branchTabsProperty.removeBranch(tab);
            project.save();
        }
        req.getSession().removeAttribute(TAB_SELECTION + project.getName());
        rsp.forwardToPreviousPage(req);
    }

    private DynamicProjectBranchTabsProperty getTabsProperty() {
        return  project.getProperty(DynamicProjectBranchTabsProperty.class);
    }

    private HistoryTab getActiveTab(Iterable<HistoryTab> tabs) {
        for(HistoryTab historyTab : tabs){
            if(historyTab.getUrl().equals(getCurrentTab())){
                return historyTab;
            }
        }
        return  Iterables.get(tabs, 0);
    }

    protected  String getCurrentTab() {
        return (String) Stapler.getCurrentRequest().getSession().getAttribute(TAB_SELECTION + project.getName());
    }
}