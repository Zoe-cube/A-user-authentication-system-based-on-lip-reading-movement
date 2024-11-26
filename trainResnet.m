%% 导入
ds = imageDatastore("D:\GraduationProject\New Folder\spectrogram\","IncludeSubfolders",true,LabelSource="foldernames");
%% 划分训练集和测试集
[trainImgs,testImgs] = splitEachLabel(ds,0.6,"randomized");
%% 格式化为224x224x3大小的
resizeTrainImgs = augmentedImageDatastore([224 224],trainImgs);
resizeTestImgs = augmentedImageDatastore([224 224],testImgs);
%% 类别数(4类)
numClasses = numel(categories(ds.Labels));
%% 创建网络
net = resnet50;
lgraph = layerGraph(net);
%% 修改(输出类别数)
newFc = fullyConnectedLayer(numClasses,"Name","new_fc");
lgraph = replaceLayer(lgraph,'fc1000',newFc);
newOut = classificationLayer("Name","new_out");
lgraph = replaceLayer(lgraph,'ClassificationLayer_fc1000',newOut);
    
%% 参数
options = trainingOptions("sgdm","InitialLearnRate",0.001);
%% 训练
[net,info] = trainNetwork(resizeTrainImgs,lgraph,options);

%% 测试
[testpreds,scores] = classify(net,resizeTestImgs)
%% 评估性能(准确率、混淆矩阵）
acc = nnz(testpreds == testImgs.Labels)/numel(testpreds)
chart = confusionchart(testImgs.Labels, testpreds);
chart.Title = 'Confusion Matrix';
chart.XLabel = 'Predicted Class';
chart.YLabel = 'True Class';
chart.ColumnSummary = 'column-normalized';
chart.RowSummary = 'row-normalized';
figure;
plotconfusion(testImgs.Labels, testpreds)
%confusionchart(testImgs.Labels,testpreds);
%% 展示迭代的准确度和损失函数
figure;
x = 1:numel(info.TrainingAccuracy)
plot(x,info.TrainingAccuracy)
grid;
xlabel("迭代次数")
ylabel("准确度")
figure;
plot(x,info.TrainingLoss)
grid;
xlabel("迭代次数")
ylabel("损失函数值")
%% 展示单个样本和预测结果

%% 置信度
 % max(scores,[],2)%max的矩阵用法
 bar(max(scores,[],2))
xticklabels(testpreds)
xtickangle(60)
ylabel("Score of Prediction")
%% ROC曲线
% trueLabels是图像分类问题的真实标签,在这里是testImgs.Labels。 scores是一个 N×K 数组，其中 N 是观测值的数量，K 是类的数量。scoresscores

% classNames = flowersData.classNames;
classNames = unique(testImgs.Labels)
% 使用 中的真实标签和 中的分类分数创建对象。指定使用 的列顺序。rocmetricstrueLabelsscoresscoresclassNames
rocObj = rocmetrics(testImgs.Labels,scores,classNames)
% rocObj是一个对象，用于在 和 属性中存储每个类的 AUC 值和性能指标。显示属性。rocmetricsAUCMetricsAUC
 auc = rocObj.AUC

% 绘制每个类的 ROC 曲线。

 plot(rocObj)
 %% 特征可视化
%  卷积层输出一个激活三维体，其中沿第三个维度的切片对应于应用到层输入的单个滤波器。网络末尾的全连接层输出的通道对应于较浅层学习的特征的高级组合。
% 
% 您可以使用 deepDreamImage 生成可强烈激活网络层特定通道的图像，从而将所学习的特征可视化。

%1.  可视化较浅的卷积层
% GoogLeNet 网络中有多个卷积层。靠近网络开头的卷积层具有较小的感受野，用于学习较小的低级特征。靠近网络末端的层具有较大的感受野，用于学习较大的特征。
% 
% 使用 analyzeNetwork 属性，查看网络架构并找到卷积层。
analyzeNetwork(net)
%%
%2.查看卷积层 1 上的特征
% 将 layer 设置为第一个卷积层。该层是网络中的第二层，名为 'conv1'。
layer = 2;
name = net.Layers(layer).Name
%%
%3.通过将 channels 设置为索引 1:36 的向量，使用 deepDreamImage 可视化该层学习的前 36 个特征。将 'PyramidLevels' 设置为 1，以避免图像缩放。要将图像显示在一起，可以使用 imtile。
% 
% 默认情况下，deepDreamImage 使用兼容的 GPU（如果可用）。否则将使用 CPU。使用 GPU 需要 Parallel Computing Toolbox™ 和支持的 GPU 设备。有关受支持设备的信息，请参阅GPU Computing Requirements (Parallel Computing Toolbox)。

channels = 1:36;
I = deepDreamImage(net,name,channels, ...
    'PyramidLevels',1);
%%
figure;
I = imtile(I,'ThumbnailSize',[64 64]);
imshow(I)
title(['Layer ',name,' Features'],'Interpreter','none')
%%这些图像主要包含边缘和颜色，指示层 'conv1-7x7_s2' 中的滤波器是边缘检测器和颜色滤波器。
%%
% 卷积层 2 上的特征
% 
% 第二个卷积层名为 'conv2-3x3_reduce'，对应于层 6。通过将 channels 设置为索引 1:36 的向量，可视化该层学习的前 36 个特征。
% 
% 要在优化过程中隐藏详细输出，请在调用 deepDreamImage. 时将 'Verbose' 设置为 'false'。
layer = 6;
name = net.Layers(layer).Name
channels = 1:36;
I = deepDreamImage(net,name,channels, ...
    'Verbose',false, ...
    'PyramidLevels',1);
figure
I = imtile(I,'ThumbnailSize',[64 64]);
imshow(I)
name = net.Layers(layer).Name;
title(['Layer ',name,' Features'],'Interpreter','none')
%该层的滤波器将检测比第一个卷积层更复杂的模式。
%% 
% 可视化较深的卷积层
% 较深的层学习较浅层学习的特征的高级组合。
% 
% 增加金字塔等级数和每个金字塔等级的迭代次数可以生成更详细的图像，但代价是额外计算。您可以使用 'NumIterations' 选项增加迭代次数，并使用 'PyramidLevels' 选项增加金字塔层级数。

layer = 98;
name = net.Layers(layer).Name
channels = 1:6;
I = deepDreamImage(net,name,channels, ...
    'Verbose',false, ...
    "NumIterations",20, ...
    'PyramidLevels',2);
figure
I = imtile(I,'ThumbnailSize',[250 250]);
imshow(I)
name = net.Layers(layer).Name;
title(['Layer ',name,' Features'],'Interpreter','none')
%请注意，越深入网络的层会产生越详细的滤波器，这些滤波器已学习了复杂的模式和纹理。
%% 
% 可视化全连接层
% 要生成最接近每个类的图像，请选择全连接层，并将 channels 设置为类的索引。
% 
% 选择全连接层（层 142）。

layer = 175;
name = net.Layers(layer).Name
%通过将 channels 设置为这些类名称的索引，选择要可视化的类。

channels = [1 2 3 4];
%这些类存储在输出层（最后一层）的 Classes 属性中。您可以通过选择 channels 中的条目来查看所选类的名称。

%生成强烈激活这些类的详细图像。在调用 deepDreamImage 时将 'NumIterations' 设置为 100，以生成更详细的图像。从全连接层生成的图像对应于图像类。
net.Layers(end).Classes(channels);
I = deepDreamImage(net,name,channels, ...
    'Verbose',false, ...
    'NumIterations',100, ...
    'PyramidLevels',2);
figure
I = imtile(I,'ThumbnailSize',[250 250]);
imshow(I)
name = net.Layers(layer).Name;
title(['Layer ',name,' Features'])