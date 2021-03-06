/**
 * Copyright 2017-2019 European Union, interactive instruments GmbH
 * Licensed under the EUPL, Version 1.2 or - as soon they will be approved by
 * the European Commission - subsequent versions of the EUPL (the "Licence");
 * You may not use this work except in compliance with the Licence.
 * You may obtain a copy of the Licence at:
 *
 * https://joinup.ec.europa.eu/software/page/eupl
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the Licence is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the Licence for the specific language governing permissions and
 * limitations under the Licence.
 *
 * This work was supported by the EU Interoperability Solutions for
 * European Public Administrations Programme (http://ec.europa.eu/isa)
 * through Action 1.17: A Reusable INSPIRE Reference Platform (ARE3NA).
 */
package de.interactive_instruments.etf.testdriver;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Path;

import javax.xml.xpath.XPathExpressionException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.interactive_instruments.etf.XmlUtils;
import de.interactive_instruments.etf.dal.dao.Dao;
import de.interactive_instruments.etf.dal.dao.StreamWriteDao;
import de.interactive_instruments.etf.dal.dto.capabilities.TestObjectTypeDto;

/**
 * @author Jon Herrmann ( herrmann aT interactive-instruments doT de )
 */
public final class TestObjectTypeBuilder implements TypeBuildingFileVisitor.TypeBuilder<TestObjectTypeDto> {

	private final StreamWriteDao<TestObjectTypeDto> writeDao;
	private final static Logger logger = LoggerFactory.getLogger(TestObjectTypeBuilder.class);
	private static final String TEST_OBJECT_TYPE_PREFIX = "TestObjectType-";
	private static final String TEST_OBJECT_TYPE_SUFFIX = ".xml";

	public TestObjectTypeBuilder(final Dao<TestObjectTypeDto> writeDao) {
		this.writeDao = (StreamWriteDao<TestObjectTypeDto>) writeDao;
	}

	private static class TestObjectTypeBuilderCmd extends TypeBuildingFileVisitor.TypeBuilderCmd<TestObjectTypeDto> {

		private final StreamWriteDao<TestObjectTypeDto> writeDao;
		private final static Logger logger = LoggerFactory.getLogger(TestObjectTypeBuilderCmd.class);

		TestObjectTypeBuilderCmd(final Path path, final StreamWriteDao<TestObjectTypeDto> writeDao)
				throws IOException, XPathExpressionException {
			super(path);
			this.writeDao = writeDao;
			this.id = XmlUtils.eval("/etf:TestObjectType[1]/@id", path.toFile());
		}

		@Override
		protected TestObjectTypeDto build() {
			try {
				final File file = path.toFile();
				final FileInputStream fileInputStream = new FileInputStream(file);
				return writeDao.add(fileInputStream);
			} catch (IOException e) {
				logger.error("Error creating Test Object Type from file {}", path, e);
			}
			return null;
		}
	}

	@Override
	public TypeBuildingFileVisitor.TypeBuilderCmd<TestObjectTypeDto> prepare(final Path path) {
		final String fName = path.getFileName().toString();
		if (fName.startsWith(TEST_OBJECT_TYPE_PREFIX) &&
				fName.endsWith(TEST_OBJECT_TYPE_SUFFIX)) {
			try {
				return new TestObjectTypeBuilderCmd(path, writeDao);
			} catch (IOException | XPathExpressionException e) {
				logger.error("Could not prepare Test Object Type {} ", path, e);
			}

		}
		return null;
	}
}
