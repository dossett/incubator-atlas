/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.atlas.repository;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.apache.atlas.AtlasException;
import org.apache.atlas.repository.memory.MemRepository;
import org.apache.atlas.typesystem.ITypedReferenceableInstance;
import org.apache.atlas.typesystem.Referenceable;
import org.apache.atlas.typesystem.Struct;
import org.apache.atlas.typesystem.types.AttributeDefinition;
import org.apache.atlas.typesystem.types.ClassType;
import org.apache.atlas.typesystem.types.DataTypes;
import org.apache.atlas.typesystem.types.HierarchicalType;
import org.apache.atlas.typesystem.types.HierarchicalTypeDefinition;
import org.apache.atlas.typesystem.types.IDataType;
import org.apache.atlas.typesystem.types.Multiplicity;
import org.apache.atlas.typesystem.types.StructType;
import org.apache.atlas.typesystem.types.StructTypeDefinition;
import org.apache.atlas.typesystem.types.TraitType;
import org.apache.atlas.typesystem.types.TypeSystem;
import org.apache.atlas.typesystem.types.utils.TypesUtil;
import org.junit.Before;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Date;
import java.util.Map;

public abstract class BaseTest {

    public static final String STRUCT_TYPE_1 = "t1";
    public static final String STRUCT_TYPE_2 = "t2";
    public static final String TEST_DATE = "2014-12-11T02:35:58.440Z";
    public static final long TEST_DATE_IN_LONG=1418265358440L;
    protected IRepository repo;

    public static Struct createStruct() throws AtlasException {
        StructType structType = (StructType) TypeSystem.getInstance()
                .getDataType(StructType.class, STRUCT_TYPE_1);
        Struct s = new Struct(structType.getName());
        s.set("a", 1);
        s.set("b", true);
        s.set("c", (byte) 1);
        s.set("d", (short) 2);
        s.set("e", 1);
        s.set("f", 1);
        s.set("g", 1L);
        s.set("h", 1.0f);
        s.set("i", 1.0);
        s.set("j", BigInteger.valueOf(1L));
        s.set("k", new BigDecimal(1));
        s.set("l", new Date(1418265358440L));
        s.set("m", Lists.<Integer>asList(Integer.valueOf(1), new Integer[]{Integer.valueOf(1)}));
        s.set("n", Lists.<BigDecimal>asList(BigDecimal.valueOf(1.1),
                new BigDecimal[]{BigDecimal.valueOf(1.1)}));
        Map<String, Double> hm = Maps.<String, Double>newHashMap();
        hm.put("a", 1.0);
        hm.put("b", 2.0);
        s.set("o", hm);
        return s;
    }

    protected final TypeSystem getTypeSystem() {
        return TypeSystem.getInstance();
    }

    protected final IRepository getRepository() {
        return repo;
    }

    @Before
    public void setup() throws Exception {

        TypeSystem ts = TypeSystem.getInstance();
        ts.reset();
        repo = new MemRepository(ts);

        StructType structType = ts.defineStructType(STRUCT_TYPE_1,
                true,
                TypesUtil.createRequiredAttrDef("a", DataTypes.INT_TYPE),
                TypesUtil.createOptionalAttrDef("b", DataTypes.BOOLEAN_TYPE),
                TypesUtil.createOptionalAttrDef("c", DataTypes.BYTE_TYPE),
                TypesUtil.createOptionalAttrDef("d", DataTypes.SHORT_TYPE),
                TypesUtil.createOptionalAttrDef("e", DataTypes.INT_TYPE),
                TypesUtil.createOptionalAttrDef("f", DataTypes.INT_TYPE),
                TypesUtil.createOptionalAttrDef("g", DataTypes.LONG_TYPE),
                TypesUtil.createOptionalAttrDef("h", DataTypes.FLOAT_TYPE),
                TypesUtil.createOptionalAttrDef("i", DataTypes.DOUBLE_TYPE),
                TypesUtil.createOptionalAttrDef("j", DataTypes.BIGINTEGER_TYPE),
                TypesUtil.createOptionalAttrDef("k", DataTypes.BIGDECIMAL_TYPE),
                TypesUtil.createOptionalAttrDef("l", DataTypes.DATE_TYPE),
                TypesUtil.createOptionalAttrDef("m", ts.defineArrayType(DataTypes.INT_TYPE)),
                TypesUtil.createOptionalAttrDef("n", ts.defineArrayType(DataTypes.BIGDECIMAL_TYPE)),
                TypesUtil.createOptionalAttrDef("o",
                        ts.defineMapType(DataTypes.STRING_TYPE, DataTypes.DOUBLE_TYPE)));

        StructType recursiveStructType = ts.defineStructType(STRUCT_TYPE_2,
                true,
                TypesUtil.createRequiredAttrDef("a", DataTypes.INT_TYPE),
                TypesUtil.createOptionalAttrDef("s", STRUCT_TYPE_2));
    }

    protected Map<String, IDataType> defineTraits(HierarchicalTypeDefinition... tDefs)
    throws AtlasException {

        return getTypeSystem().defineTraitTypes(tDefs);
    }

    /*
     * Class Hierarchy is:
     *   Department(name : String, employees : Array[Person])
     *   Person(name : String, department : Department, manager : Manager)
     *   Manager(subordinates : Array[Person]) extends Person
     *
     * Persons can have SecurityClearance(level : Int) clearance.
     */
    protected void defineDeptEmployeeTypes(TypeSystem ts) throws AtlasException {

        HierarchicalTypeDefinition<ClassType> deptTypeDef =
                TypesUtil.createClassTypeDef("Department", ImmutableList.<String>of(),
                        TypesUtil.createRequiredAttrDef("name", DataTypes.STRING_TYPE),
                        new AttributeDefinition("employees", String.format("array<%s>", "Person"),
                                Multiplicity.COLLECTION, true, "department")
                );
        HierarchicalTypeDefinition<ClassType> personTypeDef =
                TypesUtil.createClassTypeDef("Person", ImmutableList.<String>of(),
                        TypesUtil.createRequiredAttrDef("name", DataTypes.STRING_TYPE),
                        new AttributeDefinition("department",
                                "Department", Multiplicity.REQUIRED, false, "employees"),
                        new AttributeDefinition("manager",
                                "Manager", Multiplicity.OPTIONAL, false, "subordinates")
                );
        HierarchicalTypeDefinition<ClassType> managerTypeDef =
                TypesUtil.createClassTypeDef("Manager",
                        ImmutableList.<String>of("Person"),
                        new AttributeDefinition("subordinates",
                                String.format("array<%s>", "Person"),
                                Multiplicity.COLLECTION, false, "manager")
                );

        HierarchicalTypeDefinition<TraitType> securityClearanceTypeDef =
                TypesUtil.createTraitTypeDef("SecurityClearance",
                        ImmutableList.<String>of(),
                        TypesUtil.createRequiredAttrDef("level", DataTypes.INT_TYPE)
                );

        ts.defineTypes(ImmutableList.<StructTypeDefinition>of(),
                ImmutableList.<HierarchicalTypeDefinition<TraitType>>of(securityClearanceTypeDef),
                ImmutableList.<HierarchicalTypeDefinition<ClassType>>of(deptTypeDef, personTypeDef,
                        managerTypeDef));

        ImmutableList<HierarchicalType> types = ImmutableList.of(
                ts.getDataType(HierarchicalType.class, "SecurityClearance"),
                ts.getDataType(ClassType.class, "Department"),
                ts.getDataType(ClassType.class, "Person"),
                ts.getDataType(ClassType.class, "Manager")
        );

        repo.defineTypes(types);

    }

    protected Referenceable createDeptEg1(TypeSystem ts) throws AtlasException {
        Referenceable hrDept = new Referenceable("Department");
        Referenceable john = new Referenceable("Person");
        Referenceable jane = new Referenceable("Manager", "SecurityClearance");

        hrDept.set("name", "hr");
        john.set("name", "John");
        john.set("department", hrDept);
        jane.set("name", "Jane");
        jane.set("department", hrDept);

        john.set("manager", jane);

        hrDept.set("employees", ImmutableList.<Referenceable>of(john, jane));

        jane.set("subordinates", ImmutableList.<Referenceable>of(john));

        jane.getTrait("SecurityClearance").set("level", 1);

        ClassType deptType = ts.getDataType(ClassType.class, "Department");
        ITypedReferenceableInstance hrDept2 = deptType.convert(hrDept, Multiplicity.REQUIRED);

        return hrDept;
    }
}