set.seed(2016)
setwd("~/Kaggle/Depot/data")

if (!require("randomForest")) install.packages("randomForest")
if (!require("doParallel")) install.packages("doParallel")
if (!require("foreach")) install.packages("foreach")
if (!require("tm")) install.packages("tm")
#if (!require("SnowballCC")) install.packages("SnowballCC")
if (!require("SnowballC")) install.packages("SnowballC")
#if (!require("RColorBrewer")) install.packages("RColorBrewer")
#if (!require("ggplot2")) install.packages("ggplot2")
#if (!require("wordcloud")) install.packages("biclust")
if (!require("cluster")) install.packages("cluster")
if (!require("igraph")) install.packages("igraph")
if (!require("fpc")) install.packages("fpc")
if (!require("sets")) install.packages("sets")
if (!require("utils")) install.packages("utils")
library("sets")

#number of cores on the machine
ncores <- detectCores(all.tests = FALSE, logical = TRUE)
registerDoParallel(cores=ncores) 
print(paste("Working with ", getDoParWorkers(), " workers."))
#Loading data
train <- read.csv(file="train.csv", header=TRUE, sep=",")
test <- read.csv(file="test.csv", header=TRUE, sep=",")
product_descriptions <- read.csv(file="product_descriptions.csv", header=TRUE, sep=",")
attributes <- read.csv(file="attributes.csv", header=TRUE, sep=",")
search_terms <- read.csv(file="search_terms.csv", header=TRUE, sep=",")
attributes_grouped <- read.csv(file="attributes_grouped.csv", header=TRUE, sep=",")

codemeasures<-function(s){
  s<-tolower(s)
  s<-gsub("[[:punct:]]"," ", s)
  s<-gsub(" x "," xbi ", s)
  s<-gsub(" by "," xbi ", s)
  
  s<-gsub("([0-9])([a-z])", "\\1 \\2", s,  perl = TRUE)
  s<-gsub("([a-z])([0-9])", "\\1 \\2", s,  perl = TRUE)  
  s<-gsub("([0-9]+)( *)(inches|inch|in|')", "\\1 inch ", s,  perl = TRUE)
  s<-gsub("([0-9]+)( *)(foot|feet|ft|'')", "\\1 ft ", s,  perl = TRUE)
  s<-gsub("([0-9]+)( *)(pounds|pound|lbs|lb)", "\\1 lb ", s, perl = TRUE)
  s<-gsub("([0-9]+)( *)(gallons|gallon|gal)", "\\1 gal ", s, perl = TRUE)
  s<-gsub("([0-9]+)( *)(ounces|ounce|oz)", "\\1 oz ", s, perl = TRUE)
  s<-gsub("([0-9]+)( *)(centimeters|cm)","\\1 cm ", s, perl = TRUE)
  s<-gsub("([0-9]+)( *)(milimeters|mm)", "\\1 mm ", s, perl = TRUE)
  s<-gsub("°"," degrees ", s)
  s<-gsub("([0-9]+)( *)(degrees|degree)", "\\1 deg ", s, perl = TRUE)
  s<-gsub(" v "," volts ", s)
  s<-gsub("([0-9]+)( *)(volts|volt)", "\\1 volt ", s, perl = TRUE)
  s<-gsub("([0-9]+)( *)(watts|watt)", "\\1 watt ", s, perl = TRUE)
  s<-gsub("([0-9]+)( *)(amperes|ampere|amps|amp)", "\\1 amp ", s, perl = TRUE)
  return(s)
}
 

#Cleaning text
cleaning<-function(dataset){
  #Extracting measures
  dataset<-codemeasures(dataset)    
  # Creating corpus
  corpus <- Corpus(VectorSource(dataset))
  # Remove english stopwords, such as "the" and "a"
  corpus <- tm_map(corpus, removeWords, stopwords("english"))
  #Stemming
  corpus <- tm_map(corpus, stemDocument)
  #Remove space
  corpus <- tm_map(corpus, stripWhitespace)
  #Returning a dataframe
  dataframe<-data.frame(text=unlist(sapply(corpus, `[`, "content")),stringsAsFactors=F)
  return(dataframe$text)
}

train$product_title <- cleaning(train$product_title)
test$product_title <- cleaning(test$product_title)
product_descriptions$product_description <- cleaning(product_descriptions$product_description)
train$search_term <- cleaning(train$search_term)
test$search_term <- cleaning(test$search_term)
search_terms$search_term <- cleaning(search_terms$search_term)
attributes_grouped$name <- cleaning(attributes_grouped$name)
attributes_grouped$value <- cleaning(attributes_grouped$value)

strlen<-function(str1){  
  result <- 1
  str2 <- gsub(' {2,}',' ',str1)  
  
  if(!is.null(length(strsplit(str2,' ')[[1]]))){
    result <- length(strsplit(str2,' ')[[1]])
  }  
  return(result)
  
}

jaccard<-function(str1, str2){
  
  result <- 4
  
  str1 <- gsub(' {2,}',' ',str1)
  str2 <- gsub(' {2,}',' ',str2) 
  
  set1<-gset(support=unlist(strsplit(str1, split=" ")))
  set2<-gset(support=unlist(strsplit(str2, split=" ")))
  
  if(!is.null(gset_intersection(set1, set2))){
    result <- length(gset_intersection(set1, set2))
  }  
  return(result)                            
}

first_match <- function(str1, str2){
  result <- -1
  
  str1 <- gsub(' {2,}',' ',str1)
  str2 <- gsub(' {2,}',' ',str2)
  if(length(strsplit(str1, split=" ")[[1]])>0 ){
  patt <- strsplit(str1, split=" ")[[1]][1]
  result <- regexpr(paste0(patt),str2)[1]
  }
  return(result)
}
last_match <- function(str1, str2){
  result <- -1
  
  str1 <- gsub(' {2,}',' ',str1)
  str2 <- gsub(' {2,}',' ',str2)
  if(length(strsplit(str1, split=" ")[[1]])>0){
  patt <- strsplit(str1, split=" ")[[1]][length(strsplit(str1, split=" ")[[1]])]
  result <- regexpr(paste0(patt),str2)[1]
  }
  return(result)
}

inic<-Sys.time()
features_train_v6 <- foreach(i=1:length(train$search_term), .packages='sets') %dopar% {
  list(jaccard(train$search_term[[i]], train$product_title[[i]]), 
       jaccard(train$search_term[[i]], product_descriptions$product_description[product_descriptions$product_uid==train$product_uid[[i]]]),
       jaccard(train$product_title[[i]], product_descriptions$product_description[product_descriptions$product_uid==train$product_uid[[i]]]),
       jaccard(train$search_term[[i]], attributes_grouped$name[attributes_grouped$product_uid==train$product_uid[[i]]]),
       jaccard(train$search_term[[i]], attributes_grouped$value[attributes_grouped$product_uid==train$product_uid[[i]]]),
       
       
       strlen(train$search_term[[i]]),
       first_match(train$search_term[[i]], train$product_title[[i]]),
       last_match(train$search_term[[i]], train$product_title[[i]]),
       first_match(train$search_term[[i]], product_descriptions$product_description[product_descriptions$product_uid==train$product_uid[[i]]]),
       last_match(train$search_term[[i]], product_descriptions$product_description[product_descriptions$product_uid==train$product_uid[[i]]]),       
       first_match(train$search_term[[i]], attributes_grouped$name[attributes_grouped$product_uid==train$product_uid[[i]]]),
       last_match(train$search_term[[i]], attributes_grouped$name[attributes_grouped$product_uid==train$product_uid[[i]]]),       
       first_match(train$search_term[[i]], attributes_grouped$value[attributes_grouped$product_uid==train$product_uid[[i]]]),
       last_match(train$search_term[[i]], attributes_grouped$value[attributes_grouped$product_uid==train$product_uid[[i]]])       
       
       )
}
print(Sys.time()-inic)

#dfeat_train_v5[is.na(dfeat_train_v5)] <- -2

dfeat_train_v6 <- data.frame(matrix(unlist(features_train_v6), ncol=14, byrow=T),stringsAsFactors=FALSE)
dfeat_train_v6[is.na(dfeat_train_v6)] <- -2

inic<-Sys.time()
features_test_v6 <- foreach(i=1:length(test$search_term), .packages='sets') %dopar% {
  list(jaccard(test$search_term[[i]], test$product_title[[i]]), 
       jaccard(test$search_term[[i]], product_descriptions$product_description[product_descriptions$product_uid==test$product_uid[[i]]]),
       jaccard(test$product_title[[i]], product_descriptions$product_description[product_descriptions$product_uid==test$product_uid[[i]]]),
       jaccard(test$search_term[[i]], attributes_grouped$name[attributes_grouped$product_uid==test$product_uid[[i]]]),
       jaccard(test$search_term[[i]], attributes_grouped$value[attributes_grouped$product_uid==test$product_uid[[i]]]),
       
       strlen(test$search_term[[i]]),
       first_match(test$search_term[[i]],  test$product_title[[i]]),
       last_match(test$search_term[[i]], test$product_title[[i]]),
       first_match(test$search_term[[i]], product_descriptions$product_description[product_descriptions$product_uid==test$product_uid[[i]]]),
       last_match(test$search_term[[i]], product_descriptions$product_description[product_descriptions$product_uid==test$product_uid[[i]]]),       
       first_match(test$search_term[[i]], attributes_grouped$name[attributes_grouped$product_uid==test$product_uid[[i]]]),
       last_match(test$search_term[[i]], attributes_grouped$name[attributes_grouped$product_uid==test$product_uid[[i]]]),       
       first_match(test$search_term[[i]], attributes_grouped$value[attributes_grouped$product_uid==test$product_uid[[i]]]),
       last_match(test$search_term[[i]], attributes_grouped$value[attributes_grouped$product_uid==test$product_uid[[i]]])       
       
       )
       
}
print(Sys.time()-inic)

dfeat_test_v6 <- data.frame(matrix(unlist(features_test_v6), ncol=14, byrow=T),stringsAsFactors=FALSE)
dfeat_test_v6[is.na(dfeat_test_v6)] <- -2
#Code for test null records
#which(sapply(features_test, is.null))
#this code tune the random forest parameters
#tune.par<-tuneRF(dfeat_train, train$relevance, 1, ntreeTry=500, stepFactor=1, improve=0.05, trace=TRUE, plot=TRUE, doBest=FALSE)
set.seed(20)
xgb <- xgboost(data = data.matrix(dfeat_train_v6, rownames.force = NA), label = train$relevance, eta = 0.01, max_depth = 7, nround=750, subsample = 0.90, colsample_bytree = 0.85, eval_metric = "rmse", objective = "reg:linear", nthread = 4, min_child_weight=1 )
Prediction <- predict(xgb, data.matrix(dfeat_test_v6, rownames.force = NA))

submit <- data.frame(id = test$id, relevance = Prediction)
submit$relevance[submit$relevanc<1]<-1
submit$relevance[submit$relevance>3]<-3
write.csv(submit, file = "rfV7-1000eta005depth8_.csv", row.names = FALSE)
