package org.jastadd.plugin.jastaddj.refactor;

import org.jastadd.plugin.jastaddj.editor.actions.AddParameterRefactoringHandler;
import org.jastadd.plugin.jastaddj.editor.actions.ChangeParameterTypeRefactoringHandler;
import org.jastadd.plugin.jastaddj.editor.actions.EncapsulateFieldRefactoringHandler;
import org.jastadd.plugin.jastaddj.editor.actions.ExtractClassRefactoringHandler;
import org.jastadd.plugin.jastaddj.editor.actions.ExtractInterfaceRefactoringHandler;
import org.jastadd.plugin.jastaddj.editor.actions.ExtractMethodRefactoringHandler;
import org.jastadd.plugin.jastaddj.editor.actions.ExtractTempRefactoringHandler;
import org.jastadd.plugin.jastaddj.editor.actions.InlineMethodRefactoringHandler;
import org.jastadd.plugin.jastaddj.editor.actions.InlineTempRefactoringHandler;
import org.jastadd.plugin.jastaddj.editor.actions.PullUpMethodRefactoringHandler;
import org.jastadd.plugin.jastaddj.editor.actions.PushDownMethodRefactoringHandler;
import org.jastadd.plugin.jastaddj.editor.actions.RenameRefactoringHandler;
import org.jastadd.plugin.ui.AbstractBaseActionDelegate;

public class Refactorings {
	public static class RefactoringInfo<H extends AbstractBaseActionDelegate> {
		private final String id;
		private final String topMenuId;
		private final String name;
		private final String description;
		private final String menuText;
		private final String keyBinding;
		private final Class<H> handlerClass;
		
		public RefactoringInfo(String id, String name, String description,
								String menuText, String keyBinding, Class<H> handlerClass) {
			this.id = "org.jastadd.plugin.jastaddj.refactor." + id;
			this.topMenuId = this.id + "TopMenuItem";
			this.name = name;
			this.description = description;
			this.menuText = menuText;
			this.keyBinding = keyBinding;
			this.handlerClass = handlerClass;
		}
		
		public String getId() {
			return id;
		}
		
		public String getTopMenuId() {
			return topMenuId;
		}

		public String getName() {
			return name;
		}

		public String getDescription() {
			return description;
		}

		public String getMenuText() {
			return menuText;
		}
		
		public String getKeyBinding() {
			return keyBinding;
		}
		
		public AbstractBaseActionDelegate newHandler() {
			try {
				return handlerClass.newInstance();
			} catch (InstantiationException e) {
				throw new Error(e);
			} catch (IllegalAccessException e) {
				throw new Error(e);
			}
		}
	}
	
	public static final RefactoringInfo<?>[] refactorings = {
		new RefactoringInfo<EncapsulateFieldRefactoringHandler>("EncapsulateField", "Encapsulate Field", "Encapsulate Field", 
				"E&ncapsulate Field", null, EncapsulateFieldRefactoringHandler.class),
		new RefactoringInfo<ExtractClassRefactoringHandler>("ExtractClass", "Extract Class", "Extract Class", 
				"Extract &Class", null, ExtractClassRefactoringHandler.class),
		new RefactoringInfo<ExtractInterfaceRefactoringHandler>("ExtractInterface", "Extract Interface", "Extract Interface", 
				"Extract &Interface", null,	ExtractInterfaceRefactoringHandler.class),
		new RefactoringInfo<ExtractTempRefactoringHandler>("ExtractTemp", "Extract Temp", "Extract Temp", 
				"Extract &Temp", "Shift+Alt+L",	ExtractTempRefactoringHandler.class),
		new RefactoringInfo<InlineTempRefactoringHandler>("InlineTemp", "Inline Temp", "Inline Temp", 
				"Inline Temp", "Shift+Alt+I", InlineTempRefactoringHandler.class),
		new RefactoringInfo<ExtractMethodRefactoringHandler>("ExtractMethod", "Extract Method", "Extract Method", 
				"Extract &Method", "Shift+Alt+M", ExtractMethodRefactoringHandler.class),
		new RefactoringInfo<InlineMethodRefactoringHandler>("InlineMethod", "Inline Method", "Inline Method", 
				"Inline Method", null, InlineMethodRefactoringHandler.class),
		new RefactoringInfo<RenameRefactoringHandler>("Rename", "Rename", "Rename", 
				"&Rename", "Shift+Alt+R", RenameRefactoringHandler.class),
		new RefactoringInfo<PullUpMethodRefactoringHandler>("PullUpMethod", "Pull Up Method", "Pull Up Method", 
				"Pull &Up Method", null, PullUpMethodRefactoringHandler.class),
		new RefactoringInfo<PushDownMethodRefactoringHandler>("PushDownMethod", "Push Down Method", "Push Down Method", 
				"Push &Down Method", null, PushDownMethodRefactoringHandler.class),
		new RefactoringInfo<ChangeParameterTypeRefactoringHandler>("ChangeParameterType", "Change Parameter Type", "Change Parameter Type", 
				"Change Parameter Type", "Shift+Alt+C", ChangeParameterTypeRefactoringHandler.class),
		new RefactoringInfo<AddParameterRefactoringHandler>("AddParameter", "Add Parameter", "Add Parameter", 
				"Add Parameter", "Shift+Alt+A",	AddParameterRefactoringHandler.class)
	};
}
