/*******************************************************************************
 * Copyright 2018 Maximilian Stark | Dakror <mail@dakror.de>
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/

package de.dakror.common.libgdx.lml;

import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.github.czyzby.lml.parser.LmlParser;
import com.github.czyzby.lml.parser.impl.tag.actor.TableLmlTag;
import com.github.czyzby.lml.parser.tag.LmlActorBuilder;
import com.github.czyzby.lml.parser.tag.LmlTag;
import com.github.czyzby.lml.parser.tag.LmlTagProvider;

/**
 * @author Maximilian Stark | Dakror
 */
public class CustomTagProvider implements LmlTagProvider {

    private Class<? extends Actor> clazz;

    public CustomTagProvider(Class<? extends Actor> clazz) {
        this.clazz = clazz;
    }

    @Override
    public LmlTag create(LmlParser parser, LmlTag parentTag, StringBuilder rawTagData) {
        return new TableLmlTag(parser, parentTag, rawTagData) {
            @Override
            protected Actor getNewInstanceOfActor(LmlActorBuilder builder) {
                try {
                    return clazz.getConstructor(Skin.class, String.class).newInstance(getSkin(builder), builder.getStyleName());
                } catch (Exception e) {
                    e.printStackTrace();
                    return null;
                }
            }
        };
    }
}
