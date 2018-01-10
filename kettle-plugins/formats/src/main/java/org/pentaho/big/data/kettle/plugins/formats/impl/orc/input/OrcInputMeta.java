/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2017 by Hitachi Vantara : http://www.pentaho.com
 *
 *******************************************************************************
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 ******************************************************************************/
package org.pentaho.big.data.kettle.plugins.formats.impl.orc.input;

import org.pentaho.big.data.api.cluster.NamedCluster;
import org.pentaho.big.data.api.cluster.NamedClusterService;
import org.pentaho.big.data.api.cluster.service.locator.NamedClusterServiceLocator;
import org.pentaho.big.data.kettle.plugins.formats.FormatInputOutputField;
import org.pentaho.big.data.kettle.plugins.formats.orc.input.OrcInputMetaBase;
import org.pentaho.di.core.annotations.Step;
import org.pentaho.di.core.exception.KettlePluginException;
import org.pentaho.di.core.exception.KettleStepException;
import org.pentaho.di.core.injection.InjectionSupported;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.core.row.ValueMetaInterface;
import org.pentaho.di.core.row.value.ValueMetaFactory;
import org.pentaho.di.core.variables.VariableSpace;
import org.pentaho.di.repository.Repository;
import org.pentaho.di.trans.Trans;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.StepDataInterface;
import org.pentaho.di.trans.step.StepInterface;
import org.pentaho.di.trans.step.StepMeta;
import org.pentaho.metastore.api.IMetaStore;

//keep ID as new because we will have old step with ID OrcInput
@Step( id = "OrcInput", image = "OI.svg", name = "OrcInput.Name", description = "OrcInput.Description",
    categoryDescription = "i18n:org.pentaho.di.trans.step:BaseStep.Category.BigData",
    documentationUrl = "Products/Data_Integration/Transformation_Step_Reference/Orc_Input",
    i18nPackageName = "org.pentaho.di.trans.steps.orc" )
@InjectionSupported( localizationPrefix = "OrcInput.Injection.", groups = { "FILENAME_LINES", "FIELDS" }, hide = {
  "FILEMASK", "EXCLUDE_FILEMASK", "FILE_REQUIRED", "INCLUDE_SUBFOLDERS", "FIELD_POSITION", "FIELD_LENGTH",
  "FIELD_IGNORE", "FIELD_FORMAT", "FIELD_PRECISION", "FIELD_CURRENCY",
  "FIELD_DECIMAL", "FIELD_GROUP", "FIELD_REPEAT", "FIELD_TRIM_TYPE", "FIELD_NULL_STRING", "FIELD_IF_NULL",
  "FIELD_NULLABLE", "ACCEPT_FILE_NAMES", "ACCEPT_FILE_STEP", "PASS_THROUGH_FIELDS", "ACCEPT_FILE_FIELD",
  "ADD_FILES_TO_RESULT", "IGNORE_ERRORS", "FILE_ERROR_FIELD", "FILE_ERROR_MESSAGE_FIELD", "SKIP_BAD_FILES",
  "WARNING_FILES_TARGET_DIR", "WARNING_FILES_EXTENTION",
  "ERROR_FILES_TARGET_DIR", "ERROR_FILES_EXTENTION", "LINE_NR_FILES_TARGET_DIR", "LINE_NR_FILES_EXTENTION",
  "FILE_SHORT_FILE_FIELDNAME",
  "FILE_EXTENSION_FIELDNAME", "FILE_PATH_FIELDNAME", "FILE_SIZE_FIELDNAME", "FILE_HIDDEN_FIELDNAME",
  "FILE_LAST_MODIFICATION_FIELDNAME",
  "FILE_URI_FIELDNAME", "FILE_ROOT_URI_FIELDNAME", "FIELD_SOURCE_TYPE"
} )
public class OrcInputMeta extends OrcInputMetaBase {

  private final NamedClusterServiceLocator namedClusterServiceLocator;
  private final NamedClusterService namedClusterService;

  public OrcInputMeta( NamedClusterServiceLocator namedClusterServiceLocator,
      NamedClusterService namedClusterService ) {
    this.namedClusterServiceLocator = namedClusterServiceLocator;
    this.namedClusterService = namedClusterService;
  }

  @Override
  public StepInterface getStep( StepMeta stepMeta, StepDataInterface stepDataInterface, int copyNr, TransMeta transMeta,
      Trans trans ) {
    return new OrcInput( stepMeta, stepDataInterface, copyNr, transMeta, trans, namedClusterServiceLocator );
  }

  @Override
  public StepDataInterface getStepData() {
    return new OrcInputData();
  }

  public NamedCluster getNamedCluster() {
    return namedClusterService.getClusterTemplate();
  }

  public NamedClusterServiceLocator getNamedClusterServiceLocator() {
    return namedClusterServiceLocator;
  }

  @Override
  public void getFields( RowMetaInterface rowMeta, String origin, RowMetaInterface[] info, StepMeta nextStep,
                        VariableSpace space, Repository repository, IMetaStore metaStore ) throws
          KettleStepException {
    try {
      if ( !inputFiles.passingThruFields ) {
        // all incoming fields are not transmitted !
        rowMeta.clear();
      } else {
        if ( info != null ) {
          boolean found = false;
          for ( int i = 0; i < info.length && !found; i++ ) {
            if ( info[i] != null ) {
              rowMeta.mergeRowMeta( info[i], origin );
              found = true;
            }
          }
        }
      }
      for ( int i = 0; i < inputFields.length; i++ ) {
        FormatInputOutputField field = inputFields[ i ];
        String value = space.environmentSubstitute( field.getName() );
        ValueMetaInterface v = ValueMetaFactory.createValueMeta( value, field.getType() );
        v.setOrigin( origin );
        rowMeta.addValueMeta( v );
      }
    } catch ( KettlePluginException e ) {
      throw new KettleStepException( "Unable to create value type", e );
    }
  }
}