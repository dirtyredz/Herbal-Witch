package io.github.dirtyredz.herbalwitch.client.fixture;
import com.mojang.blaze3d.vertex.VertexConsumer;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;
/** Shared headless export of actual vanilla model vertices, with no renderer or GL context. */
public final class NativeVertices implements VertexConsumer {
        final String texture;public final List<Map<String,Object>> quads=new ArrayList<>();public final List<double[]> pending=new ArrayList<>(),uvs=new ArrayList<>();
        double[] position,uv,normal;
        public NativeVertices(String texture){this.texture=texture;}
        public VertexConsumer vertex(double x,double y,double z){position=new double[]{16*x,24-16*y,16*z};return this;}
        public VertexConsumer uv(float u,float v){uv=new double[]{16*u,16*v};return this;}
        public VertexConsumer normal(float x,float y,float z){normal=new double[]{x,-y,z};return this;}
        public VertexConsumer color(int r,int g,int b,int a){return this;}
        public VertexConsumer overlayCoords(int u,int v){return this;}
        public VertexConsumer uv2(int u,int v){return this;}
        public void defaultColor(int r,int g,int b,int a){}
        public void unsetDefaultColor(){}
        public void endVertex(){
            for(double value:position)assertTrue(Double.isFinite(value)&&Math.abs(value)<64,"pose escaped bounds");
            pending.add(position);uvs.add(uv);
            if(pending.size()==4){quads.add(Map.of("pos",List.copyOf(pending),"uv",List.copyOf(uvs),"normal",normal,"texture",texture,"shade",true));pending.clear();uvs.clear();}
        }
    }
