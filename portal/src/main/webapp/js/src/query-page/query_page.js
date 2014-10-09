var app = angular.module('query-page-module', ['ui.bootstrap', 'localytics.directives']);
app.directive('profileGroup', function () {
    return {
        restrict: 'A',
        replace: true,
        templateUrl: '/js/src/query-page/profileGroup.html',
    };
});
app.directive('resize', function ($window) {
    return function (scope, element, attrs) {
        var w = angular.element($window);
        scope.getWindowDimensions = function () {
            return {
                'match': $window.matchMedia('(max-width: 1200px)').matches
            };
        };
        scope.$watch(scope.getWindowDimensions, function (value) {
            if (attrs.id === "cbioportal_logo") {
                var link = "images/cbioportal_logo.png";
                if (value.match) {
                    link = "images/cbioportal_icon.png";
                } else {
                    link = "images/cbioportal_logo.png";
                }
                scope.link = function () {
                    return link;
                };
            }
        }, true);

        w.bind('resize', function () {
            scope.$apply();
        });
    };
});

app.filter('to_trusted_html', ['$sce', function ($sce) {
        return function (text) {
            return $sce.trustAsHtml(text);
        };
    }]);

app.factory('DataManager', ['$http', '$q', function ($http, $q) {
        // This handles lazy loading of data. Thus, every method returns a promise
        //  that is immediately resolved iff the requested data has already been
        //  loaded.
        // This means that to ask for data, you call the corresponding function
        //  and attach a callback to it using 'then'.
        // For example, if I wanted data about the cancer study 'stud' and I want
        //  to do 'func(data)' once I get the data, I do the following:
        //      DataManager.cancerStudy(stud).then(func);
        //  where func takes the data as an argument.


        /* Variables (all private) */
        var _typesOfCancer = {};
        var _shortNames = {};
        var _geneSets = {}; // gene set id -> gene set object
        var _geneSetStubs = {};
        var _cancerStudies = {}; // study id -> study object
        var _cancerStudyStubs = {};
        var _caseSets = {}; // case set id -> list of case ids
        var _genomicProfiles = {}; // [genomic profile id,gene,case id] -> genomic profile data
        var _samples = {};
        var initPromise = $q.defer();

        /* Initialization */
        $http.get('/portal_meta_data.json?partial_studies=true&partial_genesets=true').success(function (json) {
            angular.forEach(json.cancer_studies, function (value, key) {
                _cancerStudies[key] = value;
                _cancerStudyStubs[key] = value;
            });
            angular.forEach(json.gene_sets, function (value, key) {
                _geneSets[key] = value;
                _geneSetStubs[key] = value;
            });
            angular.forEach(json.short_names, function (value, key) {
                _shortNames[key] = value;
            });
            angular.forEach(json.type_of_cancers, function (value, key) {
                _typesOfCancer[key] = value;
            });
            initPromise.resolve();
        });

        /* Private Functions */
        var makeProfileDataPoints = function (prof_ids, genes, case_ids) {
            // helper function for genomicProfileData - should not be called anywhere else
            var ret = [];
            angular.forEach(prof_ids, function (prof_id) {
                angular.forEach(genes, function (gene) {
                    angular.forEach(case_ids, function (case_id) {
                        var elt = _genomicProfiles[prof_id][gene][case_id];
                        switch (elt.alteration_type) {
                            case "MUT":
                                if (elt.datum !== "NaN" && elt.datum !== "") {
                                    var mutations = elt.datum.split(',');
                                    for (var i = 0; i < mutations.length; i++) {
                                        ret.push({sample: case_id, gene: gene, genotype: {type: "MUT", data: mutations[i], class: "PLACEHOLDER"}});
                                    }
                                }
                                break;
                            case "CNA":
                                if (elt.datum !== "NaN" && elt.datum !== "" && elt.datum !== "0") {
                                    ret.push({sample: case_id, gene: gene, genotype: {type: "CNA", data: elt.datum}});
                                }
                                break;
                            case "EXP":
                            case "PROT":
                                if (elt.datum !== "NaN" && elt.datum !== "") {
                                    ret.push({sample: case_id, gene: gene, genotype: {type: elt.alteration_type, data: elt.datum}});
                                }
                                break;
                        }
                    });
                });
            });
            return ret;
        };

        var genomicProfileData = function (prof_ids, genes, case_ids) {
            // helper function for caseData
            // return: a list of newly loaded data points
            // 
            // figure out which we still need to load
            var toLoad = {}; //prof_id -> {genelist:list of genes, caselist:list of case_ids]
            // TODO: could do this more efficiently by covering the list of gene, case_id pairs with "rectangles" (cartesian products of sets) in the space
            //         and only loading ones we need. But its less efficient to make a ton of ajax calls than to make a single call that's bigger than we need
            angular.forEach(prof_ids, function (id) {
                toLoad[id] = {genelist: {}, caselist: {}};
            });
            angular.forEach(prof_ids, function (prof_id) {
                _genomicProfiles[prof_id] = _genomicProfiles[prof_id] || {};
                angular.forEach(genes, function (gene) {
                    _genomicProfiles[prof_id][gene] = _genomicProfiles[prof_id][gene] || {};
                    angular.forEach(case_ids, function (case_id) {
                        if (!(case_id in _genomicProfiles[prof_id][gene])) {
                            _genomicProfiles[prof_id][gene][case_id] = [];
                            toLoad[prof_id].genelist[gene] = 1;
                            toLoad[prof_id].caselist[case_id] = 1;
                        }
                    });
                });
            });
            // make into list
            angular.forEach(prof_ids, function (id) {
                toLoad[id].genelist = Object.keys(toLoad[id].genelist);
                toLoad[id].caselist = Object.keys(toLoad[id].caselist);
            });

            var newPts = [];
            // make promise and load
            var q = $q.defer();
            // count how many requests to make
            var numberToLoad = Object.keys(toLoad).length;
            // load away
            var typeCode = {"MUTATION_EXTENDED": "MUT", "COPY_NUMBER_ALTERATION": "CNA", "MRNA_EXPRESSION": "EXP", "PROTEIN_ARRAY_PROTEIN_LEVEL": "PROT"};
            var cnaEventCode = {"-2": "HOMDEL", "-1": "HETLOSS", "1": "GAIN", "2": "AMP"};
            angular.forEach(toLoad, function (obj, profile_id) {
                if (obj.genelist.length > 0) {
                    console.log("LOADING!");
                    var url = '/webservice.do?cmd=getProfileData&case_list=' + obj.caselist.join(",") +
                            '&genetic_profile_id=' + profile_id +
                            "&gene_list=" + obj.genelist.join(",");
                    $http.get(url).success(function (data, status, headers, config) {
                        var splitData = data.split('\n');
                        var sampleIds = splitData[2].split(/[\s]+/); // begin at index 2
                        var altType = typeCode[splitData[1].split(/[\s]+/)[2]];
                        for (var i = 3; i < splitData.length; i++) {
                            var row = splitData[i];
                            var cells = row.split('\t');
                            var gene = cells[1];
                            for (var j = 2; j < cells.length; j++) {
                                var sample = sampleIds[j];
                                var datum = cells[j];
                                if (altType === "CNA" && (datum in cnaEventCode)) {
                                    datum = cnaEventCode[datum];
                                }
                                _genomicProfiles[profile_id][gene][sample] = {alteration_type: altType, datum: datum}; // to be passed into makeProfileDataPoints
                            }
                        }
                        numberToLoad -= 1;
                        if (numberToLoad === 0) {
                            var newPts = [];
                            angular.forEach(prof_ids, function (id) {
                                newPts = newPts.concat(makeProfileDataPoints([id], toLoad[id].genelist, toLoad[id].caselist));
                            });
                            q.resolve(newPts);
                        }
                    });
                } else {
                    console.log("NEEDNT LOAD!");
                    numberToLoad -= 1;
                    if (numberToLoad === 0) {
                        var newPts = [];
                        angular.forEach(prof_ids, function (id) {
                            newPts = newPts.concat(makeProfileDataPoints([id], toLoad[id].genelist, toLoad[id].caselist));
                        });
                        q.resolve(newPts);
                    }
                }
            });
            if (numberToLoad === 0) {
                var newPts = [];
                angular.forEach(prof_ids, function (id) {
                    newPts = newPts.concat(makeProfileDataPoints([id], toLoad[id].genelist, toLoad[id].caselist));
                });
                q.resolve(newPts);
            }
            return q.promise;
        };

        var newSampleGeneRecord = function () {
            return {
                AMP: false,
                GAIN: false,
                HOMDEL: false,
                HETLOSS: false,
                EXP: false,
                PROT: false,
                MUT: []
            };
        }

        /* Public Functions */
        var caseData = function (prof_ids, genes, case_ids) {
            // Returns an object that's guaranteed to contain
            //  the desired cases and gene-profile data. It probably
            //  contains other data as well.
            var q = $q.defer();
            genomicProfileData(prof_ids, genes, case_ids).then(function (newDataPts) {
                angular.forEach(newDataPts, function (datum) {
                    _samples[datum.sample] = _samples[datum.sample] || {};
                    _samples[datum.sample][datum.gene] = _samples[datum.sample][datum.gene] || newSampleGeneRecord();
                    switch (datum.genotype.type) {
                        case "CNA":
                            _samples[datum.sample][datum.gene][datum.genotype.data] = true;
                            break;
                        case "EXP":
                        case "PROT":
                            _samples[datum.sample][datum.gene][datum.genotype.type] = parseFloat(datum.genotype.data);
                            break;
                    }
                    q.resolve(_samples);
                });
                if (newDataPts.length === 0) {
                    q.resolve(_samples);
                }
            });
            return q.promise;
        }

        var caseSet = function (case_set_id) {
            var q = $q.defer();
            if (case_set_id in _caseSets) {
                q.resolve(_caseSets[case_set_id]);
            } else {
                $http.get('/webservice.do?cmd=getCaseList&case_set_id=' + case_set_id).
                        success(function (data, status, headers, config) {
                            data = $.trim(data);
                            var id = data.split(/[\s]+/)[0];
                            var list = data.split(/[\s]+/).slice(1);
                            _caseSets[id] = list;
                            q.resolve(_caseSets[id]);
                        });
            }
            return q.promise;
        }
        var cancerStudy = function (id) {
            var q = $q.defer();
            if (_cancerStudies[id].partial === 'true') {
                $http.get('/portal_meta_data.json?study_id=' + id).
                        success(function (data, status, headers, config) {
                            _cancerStudies[id] = data;
                            q.resolve(_cancerStudies[id]);
                        });
            } else {
                q.resolve(_cancerStudies[id]);
            }
            return q.promise;
        };
        var geneSet = function (id) {
            var q = $q.defer();
            if (_geneSets[id].gene_list === '') {
                $http.get('/portal_meta_data.json?geneset_id=' + id.replace(/\//g,'')).
                        success(function (data, status, headers, config) {
                            _geneSets[id].gene_list = data.list;
                            q.resolve(_geneSets[id]);
                        });
            } else {
                q.resolve(_geneSets[id]);
            }
            return q.promise;
        };

        return {
            /* Variables */
            initPromise: initPromise.promise,
            /* Functions */
            typesOfCancer: function () {
                var q = $q.defer();
                initPromise.promise.then(function () {
                    q.resolve(_typesOfCancer);
                });
                return q.promise;
            },
            cancerStudyStubs: function () {
                var q = $q.defer();
                initPromise.promise.then(function () {
                    q.resolve(_cancerStudyStubs);
                });
                return q.promise;
            },
            geneSetStubs: function () {
                var q = $q.defer();
                initPromise.promise.then(function () {
                    q.resolve(_geneSetStubs);
                });
                return q.promise;
            },
            caseData: caseData,
            caseSet: caseSet,
            cancerStudy: cancerStudy,
            geneSet: geneSet
        };
    }]);

app.factory('FormVars', function () {
    var defaults = {
        cancer_study_id: 'all',
        data_priority: 'pri_mutcna',
        case_set_id: '-1',
        custom_case_list: '',
        oql_query: '',
        gene_set_id: 'user-defined-list',
        genomic_profiles: {},
        z_score_threshold: '',
        rppa_score_threshold: '',
    }
    
    return {
        cancer_study_id: 'all',
        data_priority: 'pri_mutcna',
        case_set_id: '-1',
        custom_case_list: '',
        oql_query: '',
        genomic_profiles: {},
        z_score_threshold: '',
        rppa_score_threshold: '',
        
        clear: function() {
            for(var member in defaults) {
                this[member] = defaults[member];
            }
        }
    };
});

app.factory('AppVars', ['$rootScope', 'FormVars', 'DataManager', '$q', function ($rootScope, FormVars, DataManager, $q) {
        /* CONSTANTS */
        var alt_types = ["MUTATION", "MUTATION_EXTENDED", "COPY_NUMBER_ALTERATION", "PROTEIN_LEVEL",
            "MRNA_EXPRESSION", "METHYLATION", "METHYLATION_BINARY", "PROTEIN_ARRAY_PROTEIN_LEVEL"];
        var alt_descriptions = ["Mutation", "Mutation", "Copy Number", "Protein Level", "mRNA Expression",
            "DNA Methylation", "DNA Methylation", "Protein/phosphoprotein level (by RPPA)"];

        /* VARIABLES */
        var cancer_studies = {};
        var cancer_study_stubs = {};
        var gene_set_stubs = {};
        var data_priorities = [{id: 'pri_mutcna', label: 'Mutation and CNA'}, {id: 'pri_mut', label: 'Only Mutation'}, {id: 'pri_cna', label: 'Only CNA'}];
        var types_of_cancer = [];
        var profile_groups = {};
        var ordered_profile_groups = [];
        var current_tab = 'analysis'; // 'analysis' or 'download'
        var case_sets = [];

        var vars = {
            alt_types: alt_types,
            event_types: ["AMP", "GAIN", "HETLOSS", "HOMDEL", "MUT", "EXP", "PROT"],
            data_priorities: data_priorities,
            profile_groups: profile_groups,
            ordered_profile_groups: ordered_profile_groups,
            gene_set_id: 'user-defined-list',
            error_msg: '',
            query_result: {samples: {}, genes: [], query: ''},
            types_of_cancer: types_of_cancer,
            cancer_study_stubs: cancer_study_stubs,
            cancer_studies: cancer_studies,
            case_sets: case_sets,
            gene_set_stubs: gene_set_stubs
        }

        /* FUNCTIONS */
        var updateStudyInfo = function (study_id) {
            DataManager.cancerStudy(study_id).then(function (data) {
                cancer_studies[study_id] = data;
            });
        };

        var updateProfileGroups = function (study_id) {
            var q = $q.defer();
            DataManager.cancerStudy(study_id).then(function (study) {
                // Collect by type
                ordered_profile_groups.splice(0, alt_types.length);
                for (var i = 0; i < alt_types.length; i++) {
                    profile_groups[alt_types[i]] = {alt_type: alt_types[i], description: alt_descriptions[i], list: []};
                    ordered_profile_groups.push(profile_groups[alt_types[i]]); // copy reference
                }
                for (var i = 0; i < study.genomic_profiles.length; i++) {
                    if (study.genomic_profiles[i].show_in_analysis_tab === true || current_tab === 'download') {
                        profile_groups[study.genomic_profiles[i].alteration_type].list.push(study.genomic_profiles[i]);
                    }
                }
                q.resolve();
            });
            return q.promise;
        };

        var updateCaseLists = function (study_id) {
            DataManager.cancerStudy(study_id).then(function (study) {
                vars.case_sets = study.case_sets.slice();
                vars.case_sets.push({id: '-1', name: 'User-defined case set'});
                for (var i = 0; i < vars.case_sets.length; i++) {
                    if (vars.case_sets[i].id === '-1') {
                        vars.case_sets[i].label = vars.case_sets[i].name;
                    } else {
                        vars.case_sets[i].label = vars.case_sets[i].name + ' (' + vars.case_sets[i].size + ')';
                    }
                }
            });
        }

        return {
            updateStudyInfo: updateStudyInfo,
            updateProfileGroups: updateProfileGroups,
            updateCaseLists: updateCaseLists,
            vars: vars,
        };
    }]);

app.controller('mainController2', ['$location', '$interval', '$q', '$scope', 'DataManager', 'FormVars', 'AppVars', 
                                    function ($location, $interval, $q, $scope, DataManager, FormVars, AppVars) {
        $scope.formVars = FormVars;
        $scope.appVars = AppVars;
        $scope.syncedFromUrl = false;
        $scope.range = function (n) {
            return new Array(n);
        }
        $scope.Math = Math;
        $scope.syncToUrl = function () {
            $location.path(encodeURIComponent(JSON.stringify($scope.formVars)));
        }
        $scope.syncFromUrl = function () {
            // get data from url
            if ($location.path() !== "") {
                try {
                    var pathWithoutSlash = $location.path().substring(1);
                    var decoded = JSON.parse(decodeURIComponent(pathWithoutSlash));
                    console.log(decoded);
                    for (var member in decoded) {
                        $scope.formVars[member] = decoded[member];
                    }
                    return true;
                } catch (err) {
                    return false;
                }        
            } else {
                return false;
            }
        }
        angular.element(document).ready(function () {
            // wait for datamanager to initialize before doing anything
            DataManager.initPromise.then(function () {
                $scope.syncedFromUrl = $scope.syncFromUrl();
                $interval($scope.syncToUrl, 1000);
                DataManager.typesOfCancer().then(function (toc) {
                    $scope.appVars.vars.types_of_cancer = toc;
                });
                DataManager.cancerStudyStubs().then(function (ccs) {
                    $scope.appVars.vars.cancer_study_stubs = ccs;
                });
                DataManager.geneSetStubs().then(function (gss) {
                    $scope.appVars.vars.gene_set_stubs = gss;
                });
                $scope.$watch('formVars.cancer_study_id', function () {
                    var av = $scope.appVars.vars;
                    $scope.appVars.updateStudyInfo($scope.formVars.cancer_study_id);
                    $scope.appVars.updateProfileGroups($scope.formVars.cancer_study_id).then(function () {
                        // clear selections

                        if (!$scope.syncedFromUrl) {
                            for (var i = 0; i < av.alt_types.length; i++) {
                                $scope.formVars.genomic_profiles[av.alt_types[i]] = false;
                            }
                            // make default selections
                            if (av.profile_groups["MUTATION"].list.length > 0) {
                                $scope.formVars.genomic_profiles["MUTATION"] =
                                        av.profile_groups["MUTATION"].list[0].id;
                            }
                            if (av.profile_groups["MUTATION_EXTENDED"].list.length > 0) {
                                $scope.formVars.genomic_profiles["MUTATION_EXTENDED"] =
                                        av.profile_groups["MUTATION_EXTENDED"].list[0].id;
                            }
                            if (av.profile_groups["COPY_NUMBER_ALTERATION"].list.length > 0) {
                                $scope.formVars.genomic_profiles["COPY_NUMBER_ALTERATION"] =
                                        av.profile_groups["COPY_NUMBER_ALTERATION"].list[0].id;
                            }
                    }
                    });
                    $scope.appVars.updateCaseLists($scope.formVars.cancer_study_id);
                });
                $scope.$watch('appVars.vars.gene_set_id', function() {
                    if($scope.appVars.vars.gene_set_id !== 'user-defined-list') {
                        DataManager.geneSet($scope.appVars.vars.gene_set_id).then(function(data) {
                            $scope.formVars.oql_query = data.gene_list.split(/\s+/).join("; ");
                        });
                    }
                });
            });
        });
        $scope.getCaseList = function () {
            var q = $q.defer();
            if ($scope.formVars.case_set_id === '-1') {
                q.resolve($scope.formVars.custom_case_list.split(/[,\s]+/));
            } else {
                DataManager.caseSet($scope.formVars.case_set_id).then(function (data) {
                    q.resolve(data);
                });
            }
            return q.promise;
        }

        $scope.oqlInsertDefaults = function (query) {
            var lines = query.split(/[;\n]+/);
            var retlines = [];
            for (var i=0; i<lines.length; i++) {
                var line = $.trim(lines[i]);
                var fullMatch = line.search(/^([^:\s]+)\s*(:)\s*([^\s]+).*$/);
                if (fullMatch > -1) {
                    retlines.push(line);
                } else {
                    var smallMatch = line.search(/^([^:\s]+)\s*([:]?)$/);
                    if (smallMatch > -1) {
                        // ^^ if not then we'll just throw this line out
                        var gene = RegExp.$1;
                        var specs = ""; // to be built up
                        // make a new line with the defaults
                        var defaultsToInsert = {
                            'MUT':false, 'CNA':false, 
                            'EXP':false, 'PROT': false
                        };
                        defaultsToInsert['MUT'] = ($scope.formVars.genomic_profiles['MUTATION']!==false
                                                || $scope.formVars.genomic_profiles['MUTATION_EXTENDED']!==false);
                        defaultsToInsert['CNA'] = ($scope.formVars.genomic_profiles["COPY_NUMBER_ALTERATION"]!==false);
                        defaultsToInsert['EXP'] = $scope.formVars.genomic_profiles["MRNA_EXPRESSION"]!==false;
                        defaultsToInsert['PROT'] = $scope.formVars.genomic_profiles["PROTEIN_ARRAY_PROTEIN_LEVEL"]!==false;
                        if (defaultsToInsert['MUT']) {
                            specs += ' MUT ';
                        }
                        if (defaultsToInsert['CNA']) {
                            specs += ' AMP GAIN HOMDEL HETLOSS';
                        }
                        if (defaultsToInsert['EXP']) {
                            var thresh = parseFloat($scope.formVars.z_score_threshold) || 2;
                            specs += ' EXP >= '+thresh+' EXP <= -'+thresh;
                        }
                        if (defaultsToInsert['PROT']) {
                            var thresh = parseFloat($scope.formVars.rppa_score_threshold) || 2;
                            specs += ' PROT >= '+thresh+' PROT <= -'+thresh;
                        }
                        retlines.push(gene+":"+specs);
                    }
                }
            }
            return retlines.join(";");
        }
        
        $scope.submitForm = function () {
            // definitely don't do anything until datamanager has initialized
            DataManager.initPromise.then(function () {
                // Before anything, ensure we have valid OQL
                console.log($scope.oqlInsertDefaults($scope.formVars.oql_query));
                var parsedOql = oql.parseQuery($scope.oqlInsertDefaults($scope.formVars.oql_query));
                if (parsedOql.result === 1) {
                    $scope.appVars.vars.error_msg = '';
                    angular.forEach(parsedOql.return, function (err) {
                        $scope.appVars.vars.error_msg += err.line + ': ' + err.msg + '\n';
                    });
                    return;
                }
                $scope.appVars.vars.error_msg = '';
                // General game plan: get specified genomic profiles corresponding
                //  to genes and cases.
                // Step 1: Get cases
                $scope.getCaseList().then(function (case_list) {
                    // Step 2: Get genes
                    var genes = oql.getGeneList($scope.formVars.oql_query);
                    // Step 3: Get profiles
                    var profiles = [];
                    for (var member in $scope.formVars.genomic_profiles) {
                        var val = $scope.formVars.genomic_profiles[member];
                        if (val !== false) {
                            profiles.push(val);
                        }
                    }
                    // Step 4: Make request
                    DataManager.caseData(profiles, genes, case_list).then(function (cases) {
                        // Now cases is an object that's guaranteed to contain the 
                        // desired cases and the desired gene-profile data. It may
                        // (probably will) contain more as well.
                        // Run that sucker through the oql filter
                        var qr = $scope.appVars.vars.query_result;
                        qr.query = $scope.formVars.oql_query
                        qr.genes = genes;
                        qr.samples = {};
                        var filteredIds = oql.filter(parsedOql.return, cases);
                        angular.forEach(filteredIds, function (id) {
                            qr.samples[id] = cases[id];
                        });
                    });

                });
            });
        }

    }]);