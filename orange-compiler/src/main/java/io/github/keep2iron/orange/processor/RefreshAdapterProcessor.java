package io.github.keep2iron.orange.processor;

import com.google.auto.service.AutoService;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Filer;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.Processor;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.annotation.processing.SupportedSourceVersion;
import javax.inject.Inject;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;

import io.github.keep2iron.orange.BRAVHBuildingSet;
import io.github.keep2iron.orange.RefreshBuildingSet;
import io.github.keep2iron.orange.annotations.BindOnRefresh;
import io.github.keep2iron.orange.annotations.LoadMoreAble;
import io.github.keep2iron.orange.annotations.RecyclerHolder;
import io.github.keep2iron.orange.annotations.Refreshable;
import io.github.keep2iron.orange.util.ClassUtil;
import io.github.keep2iron.orange.util.Constants;
import io.github.keep2iron.orange.util.Util;

/**
 * @author keep2iron <a href="http://keep2iron.github.io">Contract me.</a>
 * @version 1.0
 * @since 2017/11/09 18:06
 */
@AutoService(Processor.class)
@SupportedAnnotationTypes({
        "io.github.keep2iron.orange.annotations.RecyclerHolder",
        "io.github.keep2iron.orange.annotations.BindOnRefresh"
})
@SupportedSourceVersion(SourceVersion.RELEASE_7)
public class RefreshAdapterProcessor extends AbstractProcessor {
    /**
     * one @RecyclerHolder java file map into a BRAVHBuildingSet object to generate file
     */
    private ConcurrentHashMap<String, RefreshBuildingSet> mBuildingMap;
    private Elements mElementUtils;
    private Types mTypeUtils;
    private Filer mFiler;

    String refreshClassName;
    String refreshListenerClassName;
    String setRefreshListenerMethodName;

    @Override
    public synchronized void init(ProcessingEnvironment processingEnvironment) {
        super.init(processingEnvironment);

        //init utils
        mElementUtils = processingEnvironment.getElementUtils();
        mTypeUtils = processingEnvironment.getTypeUtils();
        mFiler = processingEnvironment.getFiler();
        mBuildingMap = new ConcurrentHashMap<>(50);

        Map<String, String> options = processingEnvironment.getOptions();
        refreshClassName = options.get(Constants.KEY_REFRESH_CLASS);
        refreshListenerClassName = options.get(Constants.KEY_REFRESH_LISTENER_CLASS);
        setRefreshListenerMethodName = options.get(Constants.KEY_SET_REFRESH_LISTENER_METHOD);
    }

    @Override
    public boolean process(Set<? extends TypeElement> set, RoundEnvironment roundEnvironment) {
        if (set == null || set.isEmpty()) {
            return true;
        }

        //bind @RecyclerHolder class
        bindRecyclerHolder(roundEnvironment);

        //building @BindOnRefresh
        bindOnRefresh(roundEnvironment);

        buildInject(roundEnvironment);

        for (Map.Entry<String, RefreshBuildingSet> entry : mBuildingMap.entrySet()) {
            entry.getValue().build(mFiler);
        }

        return false;
    }

    private void bindRecyclerHolder(RoundEnvironment roundEnvironment) {
        Set<? extends Element> recyclerHolderElements = roundEnvironment.getElementsAnnotatedWith(RecyclerHolder.class);
        for (Element ele : recyclerHolderElements) {
            TypeElement classFile = (TypeElement) ele;
            RefreshBuildingSet buildingSet = new RefreshBuildingSet(ele);
            mBuildingMap.put(classFile.getQualifiedName().toString(), buildingSet);
        }
    }

    private void bindOnRefresh(RoundEnvironment roundEnvironment) {
        Set<? extends Element> refreshElements = roundEnvironment.getElementsAnnotatedWith(BindOnRefresh.class);
        if (refreshElements != null &&
                refreshElements.size() != 0) {

            for (Element ele : refreshElements) {
                TypeElement classFile = (TypeElement) ele.getEnclosingElement();
                RefreshBuildingSet buildingSet = mBuildingMap.get(classFile.getQualifiedName().toString());

                if (null != buildingSet) {
                    buildingSet.bindOnRefresh(ele, refreshClassName, refreshListenerClassName, setRefreshListenerMethodName);
                }
            }
        }
    }

    public void buildInject(RoundEnvironment roundEnvironment) {
        Set<? extends Element> injectElements = roundEnvironment.getElementsAnnotatedWith(Inject.class);
        if (injectElements != null && injectElements.size() != 0) {
            for (Element ele : injectElements) {
                TypeElement classFile = (TypeElement) ele.getEnclosingElement();
                RefreshBuildingSet buildingSet = mBuildingMap.get(classFile.getQualifiedName().toString());
                if (buildingSet != null) {
                    if (ele.asType().toString().equals(Refreshable.class.getName())) {
                        buildingSet.bindInject(ele);
                    }
                }
            }
        }
    }

    @Override
    public Set<String> getSupportedAnnotationTypes() {
        HashSet<String> types = new HashSet<>();
        types.add("io.github.keep2iron.orange.annotations.BindOnRefresh");
        types.add("io.github.keep2iron.orange.annotations.RecyclerHolder");
        return types;
    }

    @Override
    public SourceVersion getSupportedSourceVersion() {
        return SourceVersion.RELEASE_7;
    }
}
