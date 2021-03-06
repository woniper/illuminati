package com.leekyoungil.illuminati.client.prossor.processor;

import com.google.auto.service.AutoService;
import com.leekyoungil.illuminati.client.annotation.Illuminati;
import com.leekyoungil.illuminati.client.prossor.properties.IlluminatiPropertiesImpl;
import com.leekyoungil.illuminati.common.properties.IlluminatiPropertiesHelper;
import com.leekyoungil.illuminati.common.util.StringObjectUtils;

import javax.annotation.processing.*;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.PackageElement;
import javax.lang.model.element.TypeElement;
import javax.tools.Diagnostic;
import javax.tools.Diagnostic.Kind;
import javax.tools.JavaFileObject;
import java.io.IOException;
import java.io.Writer;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

/**
 * Created by leekyoungil (leekyoungil@gmail.com) on 10/07/2017.
 */
@AutoService(Processor.class)
public class IlluminatiProcessor extends AbstractProcessor {

    private Filer filer;
    private Messager messager;

    private String generatedIlluminatiTemplate;

    @Override public synchronized void init(ProcessingEnvironment processingEnv) {
        super.init(processingEnv);

        this.filer = processingEnv.getFiler();
        this.messager = processingEnv.getMessager();
    }

    @Override public Set<String> getSupportedAnnotationTypes() {
        Set<String> annotataions = new LinkedHashSet<String>();
        annotataions.add(Illuminati.class.getCanonicalName());
        return annotataions;
    }

    @Override public SourceVersion getSupportedSourceVersion() {
        return SourceVersion.RELEASE_6;// SourceVersion.latestSupported();
    }

    @Override public boolean process(Set<? extends TypeElement> typeElements, RoundEnvironment env)  {
        this.messager.printMessage(Kind.WARNING, "start illuminati compile");
        outerloop:
        for (TypeElement typeElement : typeElements) {
            for (Element element : env.getElementsAnnotatedWith(typeElement)) {
                Illuminati illuminati = element.getAnnotation(Illuminati.class);

                if (illuminati != null) {
                    if (element.getKind() != ElementKind.CLASS && element.getKind() != ElementKind.METHOD) {
                        this.messager.printMessage(Kind.ERROR, "The class %s is not class or method."+ element.getSimpleName());
                        break outerloop;
                    }

                    final PackageElement pkg = processingEnv.getElementUtils().getPackageOf(element);

                    if (pkg == null) {
                        // Exception
                        this.messager.printMessage(Kind.ERROR, "Sorry, basePackage is wrong in properties read process.");
                        break outerloop;
                    }

                    if(this.setGeneratedIlluminatiTemplate(pkg.toString())) {
                        try {
                            final JavaFileObject javaFile = this.filer.createSourceFile("IlluminatiPointcutGenerated");
                            final Writer writer = javaFile.openWriter();

                            if (writer != null) {
                                writer.write(this.generatedIlluminatiTemplate);
                                writer.close();
                                this.messager.printMessage(Kind.NOTE, "generate source code!!");
                            } else {
                                this.messager.printMessage(Kind.ERROR, "Sorry, something is wrong in writer 'IlluminatiPointcutGenerated.java' process.");
                            }

                            // IlluminatiPointcutGenerated must exists only one on classloader.
                            break outerloop;
                        } catch (IOException e) {
                            this.messager.printMessage(Kind.ERROR, "Sorry, something is wrong in generated 'IlluminatiPointcutGenerated.java' process.");
                            break outerloop;
                        }
                    }
                }
            }
        }

        return true;
    }

    /**
     * Prints an error message
     *
     * @param e The element which has caused the error. Can be null
     * @param msg The error message
     */
    public void error(Element e, String msg) {
        this.messager.printMessage(Diagnostic.Kind.ERROR, msg, e);
    }

    /**
     * Generated the Illuminati Client class body.
     *
     * @param basePackageName assign a properties file setting dto. Can not be null
     * @return boolean if failed is false and another is true.
     */
    private boolean setGeneratedIlluminatiTemplate (final String basePackageName) {
        // step 1.  set basicImport
        this.generatedIlluminatiTemplate = "package {basePackageName};\r\n" + this.getImport();

        // step 2.  base package name
        this.generatedIlluminatiTemplate = this.generatedIlluminatiTemplate.replace("{basePackageName}"
                , basePackageName);

        //BrokerType brokerType = BrokerType.getEnumType(illuminatiProperties.getBroker());

        final String staticConfigurationTemplate = "     private final IlluminatiClientInit illuminatiClientInit = IlluminatiClientInit.getInstance();\r\n \r\n";

        // step 3.  properties by broker
        String implClassName;

        //if (BrokerType.RABBITMQ != brokerType && BrokerType.KAFKA != brokerType) {
            // Exception
        //    this.messager.printMessage(Kind.ERROR, "Sorry, something is wrong in properties read process.");
        //   return false;
        //}

        final String illuminatiAnnotationName = "com.leekyoungil.illuminati.client.annotation.Illuminati";
        // step 4.  check chaosBomber is activated.
        final String checkChaosBomber = IlluminatiPropertiesHelper.getPropertiesValueByKey(IlluminatiPropertiesImpl.class, this.messager, "illuminati", "chaosBomber", "false");

        String illuminatiExecuteMethod = "";

        if (StringObjectUtils.isValid(checkChaosBomber) && "true".equals(checkChaosBomber.toLowerCase())) {
            illuminatiExecuteMethod = "ByChaosBomber";
        }

        // step 5.  set the method body
        this.generatedIlluminatiTemplate += ""
                + "@Component\r\n"
                + "@Aspect\r\n"
                + "public class IlluminatiPointcutGenerated {\r\n\r\n"
                + staticConfigurationTemplate
                + "     @Pointcut(\"@within("+illuminatiAnnotationName+") || @annotation("+illuminatiAnnotationName+")\")\r\n"
                + "     public void illuminatiPointcutMethod () { }\r\n\r\n"

                + "     @Around(\"illuminatiPointcutMethod()\")\r\n"
                + "     public Object profile (ProceedingJoinPoint pjp) throws Throwable {\r\n"
                + "         if (illuminatiClientInit.checkIlluminatiIsIgnore(pjp) == true) {\r\n"
                + "             return pjp.proceed();\r\n"
                + "         }\r\n"
                + "         HttpServletRequest request = null;\r\n"
                + "         try {\r\n"
                + "             request = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest();\r\n"
                + "         } catch (Exception ex) {\r\n"
                + "             //ignore this exception.\r\n"
                + "         }\r\n"
                + "         return illuminatiClientInit.executeIlluminati"+illuminatiExecuteMethod+"(pjp, request);\r\n"
                + "     }\r\n"
                + "}\r\n"
                ;

        return true;
    }

    private String getImport () {
        final String[] illuminatis = {
                "init.IlluminatiClientInit"
        };

        final String[] aspectjs = {
                "annotation.Aspect",
                "ProceedingJoinPoint",
                "annotation.Around",
                "annotation.Pointcut"
        };

        final String[] springs = {
                "stereotype.Component",
                "web.context.request.RequestContextHolder",
                "web.context.request.ServletRequestAttributes"
        };

        final String[] blanks = {
                "javax.servlet.http.HttpServletRequest"
        };

        final Map<String, String[]> imports = new HashMap<String, String[]>();
        imports.put("com.leekyoungil.illuminati.client.prossor", illuminatis);
        imports.put("org.aspectj.lang", aspectjs);
        imports.put("org.springframework", springs);
        imports.put("", blanks);

        final StringBuilder importString = new StringBuilder();

        for (Map.Entry<String, String[]> entry : imports.entrySet() ) {
            for (String importLib : entry.getValue()) {
                importString.append("import ");
                importString.append(entry.getKey());

                if (!"".equals(entry.getKey())) {
                    importString.append(".");
                }

                importString.append(importLib);
                importString.append(";\r\n");
            }
        }

        return importString.toString();
    }
}
