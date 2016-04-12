# feed the seed :)

library("randomForest")
library("reshape")
library("lattice")
combi <- read.csv(file="data.csv", header=TRUE, sep=",")
date_columns=c("primer_efecto_2_time", "efecto_actual_2_time", "vencimiento_actual_2_time", "FR", "FR_1ano_time")
#as.Date(combi[names(combi) %in% date_columns],"%d/%m/%Y")
combi[date_columns] <- lapply(combi[date_columns], as.Date, format="%d/%m/%Y")
combi[date_columns] <- lapply(combi[date_columns], as.numeric)
combi$edad <- round(as.numeric(combi$edad)/1000)
combi$Culpa=as.numeric(combi$Sum.Culpa.>0)



fit <- randomForest(as.factor(Culpa) ~ polizas_del_cliente + canal+ Antiguetat_Anys + Tenencia_Autos+ Tenencia_Salut+ Tenencia_Llar+ Tenencia_Vida_Accidents+ prima_Anual+ forma_pago+ primer_efecto_2_time+ efecto_actual_2_time+ vencimiento_actual_2_time+ time.diff+ baja+ FR+ FR_1ano_time+ sexo+ tipo_cliente+ edad+ cod_post_modif+ localidad_modif+ Sum.Mail.comercial.+ Sum.SMS.Comercial.+ visitas_total_del_cliente, data = combi, importance=TRUE, ntree=500)
importancia=data.frame(importance(fit))
importancia=importancia[!row.names(importancia) %in% c("cli_id", "pol_id", "Sum.Reclamación.", "Sum.Indeterminado.", "Sum.Compartida.", "Sum.Culpa.","Culpa", "canal", "forma_pago","cod_post_modif", "localidad_modif", "sexo", "tipo_cliente"),]

gestudio=combi[combi$Culpa>0,]
greferencia=combi[combi$Culpa==0,]

nums=sapply(gestudio, is.numeric)

gestudionum=gestudio[nums]
greferencianum=greferencia[nums]


meanestudio=sapply(gestudionum, mean)
meanreferencia=sapply(greferencianum, mean)

meandiff=(meanestudio-meanreferencia)/meanreferencia

DiferenciaMediasNormalizadas=meandiff[!names(meandiff) %in% c("cli_id", "pol_id", "Sum.Reclamación.", "Sum.Indeterminado.", "Sum.Compartida.", "Sum.Culpa.","Culpa")]

importancia$DiffNormMean=DiferenciaMediasNormalizadas

outtable=data.frame(matrix(ncol=24, nrow=11))
for(i in 1:24){outtable[i]=unlist(t.test(gestudionum[[i]], greferencianum[[i]]))}

trans=t(outtable)
trans=data.frame(trans)

myrow <- unlist(t.test(1:10,1:10))
names(trans) <-names(myrow)

row.names(trans)=names(gestudionum)[1:24]
trans=trans[row.names(trans) %in% row.names(importancia),]

importancia_joined=cbind(importancia, trans)
importancia_join_ordered <- sort_df(importancia_joined,vars='MeanDecreaseGini')

DiferenciaMediasNormalizadas=importancia_join_ordered $DiffNormMean

names(DiferenciaMediasNormalizadas)<-row.names(importancia_join_ordered )
barchart(DiferenciaMediasNormalizadas)

View(importancia_join_ordered)




