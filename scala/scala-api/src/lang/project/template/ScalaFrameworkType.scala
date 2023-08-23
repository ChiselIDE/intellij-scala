package org.jetbrains.plugins.scala.project.template

import com.intellij.framework.FrameworkTypeEx
import com.intellij.framework.addSupport.{FrameworkSupportInModuleConfigurable, FrameworkSupportInModuleProvider}
import com.intellij.ide.util.frameworkSupport.FrameworkSupportModel
import com.intellij.ide.util.projectWizard.ModuleBuilder
import com.intellij.openapi.module.{Module, ModuleType, ModuleTypeId}
import com.intellij.openapi.roots.ui.configuration.FacetsProvider
import com.intellij.openapi.roots.{ModifiableModelsProvider, ModifiableRootModel}
import org.jetbrains.plugins.scala.{NlsString, ScalaLanguage}
import org.jetbrains.plugins.scala.icons.Icons
import org.jetbrains.plugins.scala.project.{ModuleExt, ScalaLibraryType}

//noinspection TypeAnnotation
final class ScalaFrameworkType extends FrameworkTypeEx(ScalaLanguage.INSTANCE.getID) {

  override def getIcon = Icons.SCALA_SMALL_LOGO

  override def getPresentableName = NlsString.force(getId)

  override def createProvider = new FrameworkSupportInModuleProvider {

    override def getFrameworkType = ScalaFrameworkType.this

    override def getIcon = getFrameworkType.getIcon

    override def isEnabledForModuleType(moduleType: ModuleType[_ <: ModuleBuilder]): Boolean =
      moduleType.getId match {
        case ModuleTypeId.JAVA_MODULE |
             "PLUGIN_MODULE" => true // PluginModuleType.getInstance.getId
        case _ => false
      }

    override def isSupportAlreadyAdded(module: Module,
                                       facetsProvider: FacetsProvider): Boolean = module.hasScala

    override def createConfigurable(model: FrameworkSupportModel) = new FrameworkSupportInModuleConfigurable {

      override def createComponent = null

      override def createLibraryDescription = ScalaLibraryType.Description

      override def isOnlyLibraryAdded = true

      override def addSupport(module: Module,
                              rootModel: ModifiableRootModel,
                              modifiableModelsProvider: ModifiableModelsProvider): Unit = {}
    }
  }
}
