#include <jni.h>
#include <string>
#include <vector>
#include <thread>
#include "llama.h"

static llama_model *model = nullptr;
static llama_context *ctx = nullptr;
static const llama_vocab *vocab = nullptr;

extern "C" JNIEXPORT jstring JNICALL Java_com_jfdedit3_androllm_LocalEngine_load(JNIEnv *env,jclass,jstring path) {
    if(ctx){llama_free(ctx);ctx=nullptr;}
    if(model){llama_model_free(model);model=nullptr;}
    llama_backend_init();
    const char *p=env->GetStringUTFChars(path,nullptr);
    auto mp=llama_model_default_params();
    model=llama_model_load_from_file(p,mp);
    env->ReleaseStringUTFChars(path,p);
    if(!model)return env->NewStringUTF("Impossible de charger ce GGUF");
    auto cp=llama_context_default_params();
    cp.n_ctx=2048;cp.n_batch=512;cp.n_threads=4;cp.n_threads_batch=4;
    ctx=llama_init_from_model(model,cp);
    if(!ctx){llama_model_free(model);model=nullptr;return env->NewStringUTF("Mémoire insuffisante pour ce modèle");}
    vocab=llama_model_get_vocab(model);
    return env->NewStringUTF("");
}
extern "C" JNIEXPORT jstring JNICALL Java_com_jfdedit3_androllm_LocalEngine_generate(JNIEnv *env,jclass,jstring prompt,jint maxTokens) {
    if(!ctx)return env->NewStringUTF("Charge d'abord un modèle GGUF.");
    const char *p=env->GetStringUTFChars(prompt,nullptr);
    int n=llama_tokenize(vocab,p,(int)strlen(p),nullptr,0,true,true);
    std::vector<llama_token> tokens(-n);
    int count=llama_tokenize(vocab,p,(int)strlen(p),tokens.data(),(int)tokens.size(),true,true);
    env->ReleaseStringUTFChars(prompt,p);
    if(count<=0 || count>1800)return env->NewStringUTF("Prompt trop long ou invalide.");
    llama_memory_clear(llama_get_memory(ctx),true);
    llama_batch batch=llama_batch_init(512,0,1);
    int pos=0;int result=0;
    for(int offset=0;offset<count;offset+=512){
        batch.n_tokens=0;
        int end=std::min(count,offset+512);
        for(int i=offset;i<end;i++){
            int k=batch.n_tokens++;
            batch.token[k]=tokens[i];batch.pos[k]=pos++;batch.n_seq_id[k]=1;batch.seq_id[k][0]=0;
            batch.logits[k]=(i==count-1);
        }
        result=llama_decode(ctx,batch);
        if(result!=0)break;
    }
    std::string answer;
    if(result==0){
        auto *sampler=llama_sampler_init_greedy();
        for(int i=0;i<std::min((int)maxTokens,256) && pos<2047;i++){
            llama_token token=llama_sampler_sample(sampler,ctx,-1);
            if(llama_vocab_is_eog(vocab,token))break;
            char piece[512];
            int len=llama_token_to_piece(vocab,token,piece,sizeof(piece),0,true);
            if(len>0)answer.append(piece,len);
            batch.n_tokens=1;batch.token[0]=token;batch.pos[0]=pos++;
            batch.n_seq_id[0]=1;batch.seq_id[0][0]=0;batch.logits[0]=true;
            if(llama_decode(ctx,batch)!=0)break;
        }
        llama_sampler_free(sampler);
    }else answer="Erreur pendant le traitement du modèle.";
    llama_batch_free(batch);
    return env->NewStringUTF(answer.c_str());
}
